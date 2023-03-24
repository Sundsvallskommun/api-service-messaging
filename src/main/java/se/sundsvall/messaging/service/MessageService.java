package se.sundsvall.messaging.service;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.control.Try.ofCallable;
import static se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod.NO_CONTACT;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NO_FEEDBACK_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_FEEDBACK_WANTED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.mapper.DtoMapper;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;

import lombok.Generated;

@Service
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private static final Gson GSON = new GsonBuilder().create();

    private final TransactionTemplate transactionTemplate;
    private final DbIntegration dbIntegration;
    private final FeedbackSettingsIntegration feedbackSettings;
    private final SmsSenderIntegration smsSender;
    private final EmailSenderIntegration emailSender;
    private final DigitalMailSenderIntegration digitalMailSender;
    private final WebMessageSenderIntegration webMessageSender;
    private final SnailMailSenderIntegration snailmailSender;
    private final MessageMapper messageMapper;
    private final RequestMapper requestMapper;
    private final DtoMapper dtoMapper;

    public MessageService(final TransactionTemplate transactionTemplate,
            final DbIntegration dbIntegration,
            final FeedbackSettingsIntegration feedbackSettings,
            final SmsSenderIntegration smsSender,
            final EmailSenderIntegration emailSender,
            final DigitalMailSenderIntegration digitalMailSender,
            final WebMessageSenderIntegration webMessageSender,
            final SnailMailSenderIntegration snailmailSender,
            final MessageMapper messageMapper,
            final RequestMapper requestMapper,
            final DtoMapper dtoMapper) {
        this.transactionTemplate = transactionTemplate;
        this.dbIntegration = dbIntegration;
        this.feedbackSettings = feedbackSettings;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
        this.digitalMailSender = digitalMailSender;
        this.webMessageSender = webMessageSender;
        this.snailmailSender = snailmailSender;
        this.messageMapper = messageMapper;
        this.requestMapper = requestMapper;
        this.dtoMapper = dtoMapper;
    }

    public InternalDeliveryResult sendSms(final SmsRequest request) {
        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryResult sendEmail(final EmailRequest request) {
        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryResult sendWebMessage(final WebMessageRequest request) {
        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryBatchResult sendDigitalMail(final DigitalMailRequest request) {
        var batchId = UUID.randomUUID().toString();
        // Save the message(s)
        var entities = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));
        // Deliver them
        var deliveries = entities.stream()
            .map(this::deliver)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult sendSnailMail(final SnailMailRequest request) {
        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryBatchResult sendMessages(final MessageRequest request) {
        var batchId = UUID.randomUUID().toString();
        var entities = request.messages().stream()
            .map(message -> messageMapper.toMessage(batchId, message))
            .map(dbIntegration::saveMessage)
            .toList();

        var deliveries = new ArrayList<InternalDeliveryResult>();

        entities.forEach(message -> {
            var partyId = message.partyId();

            // Get the message headers
            var headers = GSON.fromJson(message.content(), MessageRequest.Message.class).headers();

            // Attempt to get feedback settings and maybe act upon them
            var feedbackChannels = feedbackSettings.getSettingsByPartyId(headers, partyId);
            if (feedbackChannels.isEmpty()) {
                LOG.info("No feedback settings found for {}", partyId);

                // No feedback settings found - can't do anything more here
                archiveMessage(message.withStatus(NO_FEEDBACK_SETTINGS_FOUND));

                deliveries.add(new InternalDeliveryResult(message, NO_FEEDBACK_SETTINGS_FOUND));
            } else {
                for (var feedbackChannel : feedbackChannels) {
                    // Determine the contact method, if any
                    var actualContactMethod = Optional.ofNullable(feedbackChannel.contactMethod())
                        .map(contactMethod -> {
                            if (!feedbackChannel.feedbackWanted()) {
                                return NO_CONTACT;
                            }

                            return contactMethod;
                        })
                        .orElse(ContactMethod.UNKNOWN);

                    // Re-map the delivery to use the actual contact method and deliver it
                    switch (actualContactMethod) {
                        case EMAIL -> {
                            var deliveryId = UUID.randomUUID().toString();

                            LOG.info("Handling incoming message {} as e-mail with delivery id {}", message.messageId(), deliveryId);

                            var delivery = message
                                .withDeliveryId(deliveryId)
                                .withType(EMAIL)
                                .withContent(requestMapper.toEmailRequest(message, feedbackChannel.destination()));

                            // Save the re-mapped delivery
                            dbIntegration.saveMessage(delivery);
                            // Delete the original delivery
                            dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                            deliveries.add(deliver(delivery));
                        }
                        case SMS -> {
                            var deliveryId = UUID.randomUUID().toString();

                            LOG.info("Handling incoming message {} as SMS with delivery id {}", message.messageId(), deliveryId);

                            var delivery = message
                                .withDeliveryId(deliveryId)
                                .withType(SMS)
                                .withContent(requestMapper.toSmsRequest(message, feedbackChannel.destination()));

                            // Save the re-mapped delivery
                            dbIntegration.saveMessage(delivery);
                            // Delete the original delivery
                            dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                            deliveries.add(deliver(delivery));
                        }
                        case NO_CONTACT -> {
                            LOG.info("No feedback wanted for {}. No delivery will be attempted", partyId);

                            archiveMessage(message.withStatus(NO_FEEDBACK_WANTED));

                            deliveries.add(new InternalDeliveryResult(message, NO_FEEDBACK_WANTED));
                        }
                        default -> {
                            LOG.warn("Unknown/missing contact method for message {} and delivery id {} - will not be delivered",
                                message.messageId(), message.deliveryId());

                            archiveMessage(message.withStatus(FAILED));

                            deliveries.add(new InternalDeliveryResult(message, FAILED));
                        }
                    }
                }
            }
        });

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryBatchResult sendLetter(final LetterRequest request) {
        var currentMessage = new ThreadLocal<Message>();
        var batchId = UUID.randomUUID().toString();
        var entities = messageMapper.toMessages(request, batchId).stream()
            .map(dbIntegration::saveMessage)
            .toList();

        var deliveries = entities.stream()
            .map(message -> ofCallable(() -> {
                    // Try to deliver as digital mail
                    var digitalMailRequest = requestMapper.toDigitalMailRequest(request);
                    if (digitalMailRequest.attachments().isEmpty()) {
                        LOG.info("No attachment(s) for DIGITAL_MAIL - switching over to snail-mail");

                        // No attachments intended for digital mail delivery - "switch" to snail-mail
                        throw new NoLetterAttachmentsException();
                    }

                    // Re-map the message as digital mail and attempt to deliver it
                    var reroutedMessage = dbIntegration.saveMessage(message
                        .withDeliveryId(UUID.randomUUID().toString())
                        .withType(DIGITAL_MAIL)
                        .withContent(GSON.toJson(digitalMailRequest)));

                    return deliver(reroutedMessage);
                })
                .mapTry(deliveryResult -> {
                    // Success - delete the original delivery
                    dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                    return deliveryResult;
                })
                .recover(Exception.class, outerRecoveryIgnored -> ofCallable(() -> {
                        // Failure using digital mail - try to deliver as snail-mail
                        var snailMailRequest = requestMapper.toSnailMailRequest(request);
                        if (snailMailRequest.attachments().isEmpty()) {
                            LOG.info("No attachment(s) for SNAIL_MAIL - unable to send letter");

                            // No attachments intended for digital mail delivery - fail
                            throw new NoLetterAttachmentsException();
                        }

                        // Re-map the message as snail-mail and attempt to deliver it
                        var reroutedMessage = message
                            .withDeliveryId(UUID.randomUUID().toString())
                            .withType(SNAIL_MAIL)
                            .withContent(GSON.toJson(snailMailRequest));

                        // "Store" the message, for recovery handling
                        currentMessage.set(reroutedMessage);

                        return deliver(reroutedMessage);
                    })
                    .mapTry(deliveryResult -> {
                        // Success - delete the original delivery
                        dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                        return deliveryResult;
                    })
                    .recover(Exception.class, innerRecoveryIgnored -> {
                        // Failure - get the current message being delivered
                        var failedReroutedMessage = currentMessage.get();
                        currentMessage.remove();

                        // Delete the original delivery
                        dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                        return new InternalDeliveryResult(failedReroutedMessage.withStatus(FAILED));
                    })
                    .get())
                .get())
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    InternalDeliveryResult deliver(final Message delivery) {
        // Re-construct the original request
        var request = GSON.fromJson(delivery.content(), switch (delivery.type()) {
            case SMS -> SmsRequest.class;
            case EMAIL -> EmailRequest.class;
            case DIGITAL_MAIL -> DigitalMailRequest.class;
            case WEB_MESSAGE -> WebMessageRequest.class;
            case SNAIL_MAIL -> SnailMailRequest.class;
            case LETTER -> LetterRequest.class;
            default -> throw new IllegalArgumentException("Unknown request type: " + delivery.type());
        });

        // Get the try call to start out with
        var sendTry = switch (delivery.type()) {
            case SMS -> ofCallable(() ->
                smsSender.sendSms(dtoMapper.toSmsDto((SmsRequest) request)));
            case EMAIL -> ofCallable(() ->
                emailSender.sendEmail(dtoMapper.toEmailDto((EmailRequest) request)));
            case DIGITAL_MAIL -> ofCallable(() ->
                digitalMailSender.sendDigitalMail(dtoMapper.toDigitalMailDto((DigitalMailRequest) request, delivery.partyId())));
            case WEB_MESSAGE -> ofCallable(() ->
                webMessageSender.sendWebMessage(dtoMapper.toWebMessageDto((WebMessageRequest) request)));
            case SNAIL_MAIL -> ofCallable(() ->
                snailmailSender.sendSnailmail(dtoMapper.toSnailmailDto((SnailMailRequest) request)));
            default -> throw new IllegalArgumentException("Unknown delivery type: " + delivery.type());
        };

        return sendTry
            .peek(status -> {
                // Archive the message
                archiveMessage(delivery.withStatus(status));
            })
            // Map to result
            .map(status -> new InternalDeliveryResult(delivery.messageId(), delivery.deliveryId(), delivery.type(), status))
            // Make sure all exceptions that may occur are throwable problems
            .mapFailure(
                Case($(instanceOf(ThrowableProblem.class)), throwableProblem -> throwableProblem),
                Case($(), e -> {
                    LOG.info("Unable to deliver {}: {}", delivery.type(), e.getMessage());

                    return Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to deliver " + delivery.type());
                })
            )
            .toEither()
            // If we have a left (i.e. an exception has occurred) - archive the message and throw
            .peekLeft(throwable -> {
                // Archive the message
                archiveMessage(delivery.withStatus(FAILED));

                throw (ThrowableProblem) throwable;
            })
            .get();
    }

    void archiveMessage(final Message message) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NotNull final TransactionStatus status) {
                LOG.info("Moving {} delivery {} with status {} to history", message.type(),
                    message.deliveryId(), message.status());

                dbIntegration.saveHistory(message);
                dbIntegration.deleteMessageByDeliveryId(message.deliveryId());
            }
        });
    }

    @Generated // To avoid having to implement a stupid test for no reason...
    static class NoLetterAttachmentsException extends RuntimeException { }
}
