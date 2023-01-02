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
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
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
import se.sundsvall.messaging.model.DeliveryBatchResult;
import se.sundsvall.messaging.model.DeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;

import io.vavr.control.Try;

@Service
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private static final Gson GSON = new GsonBuilder().create();

    private final DbIntegration dbIntegration;
    private final FeedbackSettingsIntegration feedbackSettings;
    private final SmsSenderIntegration smsSender;
    private final EmailSenderIntegration emailSender;
    private final DigitalMailSenderIntegration digitalMailSender;
    private final WebMessageSenderIntegration webMessageSender;
    private final SnailMailSenderIntegration snailmailSender;
    private final MessageMapper mapper;

    public MessageService(final DbIntegration dbIntegration,
            final FeedbackSettingsIntegration feedbackSettings,
            final SmsSenderIntegration smsSender,
            final EmailSenderIntegration emailSender,
            final DigitalMailSenderIntegration digitalMailSender,
            final WebMessageSenderIntegration webMessageSender,
            final SnailMailSenderIntegration snailmailSender,
            final MessageMapper mapper) {
        this.dbIntegration = dbIntegration;
        this.feedbackSettings = feedbackSettings;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
        this.digitalMailSender = digitalMailSender;
        this.webMessageSender = webMessageSender;
        this.snailmailSender = snailmailSender;
        this.mapper = mapper;
    }

    @TransactionalEventListener(value = IncomingMessageEvent.class, fallbackExecution = true)
    void handleIncomingMessageEvent(final IncomingMessageEvent event) {
        // Get the message (delivery)
        var message = dbIntegration.getMessageByDeliveryId(event.getDeliveryId())
            .orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR,
                "Unable to send " + event.getMessageType() + " with id " + event.getDeliveryId()));
        // Deliver it
        deliver(message);
    }

    public DeliveryResult sendSms(final SmsRequest request) {
        // Save the message
        var message = dbIntegration.saveMessage(mapper.toMessage(request));
        // Deliver it
        return deliver(message);
    }

    public DeliveryResult sendEmail(final EmailRequest request) {
        // Save the message
        var message = dbIntegration.saveMessage(mapper.toMessage(request));
        // Deliver it
        return deliver(message);
    }

    public DeliveryResult sendWebMessage(final WebMessageRequest request) {
        // Save the message
        var message = dbIntegration.saveMessage(mapper.toMessage(request));
        // Deliver it
        return deliver(message);
    }

    public DeliveryBatchResult sendDigitalMail(final DigitalMailRequest request) {
        var batchId = UUID.randomUUID().toString();
        // Save the message(s)
        var entities = dbIntegration.saveMessages(mapper.toMessages(request, batchId));
        // Deliver them
        var deliveries = entities.stream()
            .map(this::deliver)
            .toList();

        return new DeliveryBatchResult(batchId, deliveries);
    }

    public DeliveryResult sendSnailMail(final SnailMailRequest request) {
        // Save the message
        var message = dbIntegration.saveMessage(mapper.toMessage(request));
        // Deliver it
        return deliver(message);
    }

    public DeliveryBatchResult sendMessages(final MessageRequest request) {
        var batchId = UUID.randomUUID().toString();
        var entities = request.messages().stream()
            .map(message -> mapper.toMessage(batchId, message))
            .map(dbIntegration::saveMessage)
            .toList();

        var deliveries = new ArrayList<DeliveryResult>();

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

                deliveries.add(new DeliveryResult(message, NO_FEEDBACK_SETTINGS_FOUND));
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
                                .withContent(mapper.toEmailRequest(message, feedbackChannel.destination()));

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
                                .withContent(mapper.toSmsRequest(message, feedbackChannel.destination()));

                            // Save the re-mapped delivery
                            dbIntegration.saveMessage(delivery);
                            // Delete the original delivery
                            dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                            deliveries.add(deliver(delivery));
                        }
                        case NO_CONTACT -> {
                            LOG.info("No feedback wanted for {}. No delivery will be attempted", partyId);

                            archiveMessage(message.withStatus(NO_FEEDBACK_WANTED));

                            deliveries.add(new DeliveryResult(message, NO_FEEDBACK_WANTED));
                        }
                        default -> {
                            LOG.warn("Unknown/missing contact method for message {} and delivery id {} - will not be delivered",
                                message.messageId(), message.deliveryId());

                            archiveMessage(message.withStatus(FAILED));

                            deliveries.add(new DeliveryResult(message, FAILED));
                        }
                    }
                }
            }
        });

        return new DeliveryBatchResult(batchId, deliveries);
    }

    public DeliveryBatchResult sendLetter(final LetterRequest request) {
        var batchId = UUID.randomUUID().toString();
        var entities = mapper.toMessages(request, batchId).stream()
            .map(dbIntegration::saveMessage)
            .toList();

        var deliveries = entities.stream()
            // First, try to deliver as digital mail
            .map(message -> Try.ofCallable(() -> {
                    var reroutedMessage = dbIntegration.saveMessage(message
                        .withDeliveryId(UUID.randomUUID().toString())
                        .withType(DIGITAL_MAIL)
                        .withContent(GSON.toJson(mapper.toDigitalMailRequest(request))));

                    return deliver(reroutedMessage);
                })
                .mapTry(deliveryResult -> {
                    // Archive the original message
                    archiveMessage(message.withStatus(deliveryResult.status()));

                    return deliveryResult;
                })
                // If digital mail fails, try to deliver as snail-mail
                .recover(Exception.class, ignored -> Try.ofCallable(() -> {
                        var reroutedMessage = message
                            .withDeliveryId(UUID.randomUUID().toString())
                            .withType(SNAIL_MAIL)
                            .withContent(GSON.toJson(mapper.toSnailMailRequest(request)));

                        // Register a LETTER failover
                        incrementCounter(LETTER.toString().toLowerCase() + ".failover");

                        return deliver(reroutedMessage);
                    })
                    .mapTry(deliveryResult -> {
                        // Archive the original message
                        archiveMessage(message.withStatus(deliveryResult.status()));

                        return deliveryResult;
                    })
                    .get())
                .get())
            .toList();

        return new DeliveryBatchResult(batchId, deliveries);
    }

    DeliveryResult deliver(final Message delivery) {
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
                smsSender.sendSms(mapper.toSmsDto((SmsRequest) request)));
            case EMAIL -> ofCallable(() ->
                emailSender.sendEmail(mapper.toEmailDto((EmailRequest) request)));
            case DIGITAL_MAIL -> ofCallable(() ->
                digitalMailSender.sendDigitalMail(mapper.toDigitalMailDto((DigitalMailRequest) request, delivery.partyId())));
            case WEB_MESSAGE -> ofCallable(() ->
                webMessageSender.sendWebMessage(mapper.toWebMessageDto((WebMessageRequest) request)));
            case SNAIL_MAIL -> ofCallable(() ->
                snailmailSender.sendSnailmail(mapper.toSnailmailDto((SnailMailRequest) request)));
            default -> throw new IllegalArgumentException("Unknown delivery type: " + delivery.type());
        };

        // Register a delivery attempt
        incrementAttemptCounter(delivery.type());

        return sendTry
            .peek(status -> {
                // Archive the message
                archiveMessage(delivery.withStatus(status));
                // Increment success counter
                incrementSuccessCounter(delivery.type());
            })
            // Map to result
            .map(status -> new DeliveryResult(delivery.messageId(), delivery.deliveryId(), status))
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
                // Increment failure counter
                incrementFailureCounter(delivery.type());

                throw (ThrowableProblem) throwable;
            })
            .get();
    }

    @Transactional
    void archiveMessage(final Message message) {
        LOG.info("Moving {} delivery {} with status {} to history", message.type(),
            message.deliveryId(), message.status());

        dbIntegration.saveHistory(message);
        dbIntegration.deleteMessageByDeliveryId(message.deliveryId());
    }

    void incrementAttemptCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".total");
    }

    void incrementSuccessCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".success");
    }

    void incrementFailureCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".failure");
    }

    void incrementCounter(final String name) {
        dbIntegration.incrementAndSaveCounter(name);
    }
}
