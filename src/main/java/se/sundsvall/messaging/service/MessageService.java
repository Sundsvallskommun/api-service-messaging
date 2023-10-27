package se.sundsvall.messaging.service;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.control.Try.ofCallable;
import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.integration.contactsettings.ContactDto.ContactMethod.NO_CONTACT;
import static se.sundsvall.messaging.integration.contactsettings.ContactDto.ContactMethod.UNKNOWN;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.util.JsonUtils.fromJson;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.contactsettings.ContactSettingsIntegration;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.slack.SlackIntegration;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.mapper.DtoMapper;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;

@Service
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private final TransactionTemplate transactionTemplate;
    private final BlacklistService blacklistService;
    private final DbIntegration dbIntegration;
    private final ContactSettingsIntegration contactSettingsIntegration;
    private final SmsSenderIntegration smsSender;
    private final EmailSenderIntegration emailSender;
    private final DigitalMailSenderIntegration digitalMailSender;
    private final WebMessageSenderIntegration webMessageSender;
    private final SnailMailSenderIntegration snailmailSender;
    private final SlackIntegration slackIntegration;

    private final MessageMapper messageMapper;
    private final RequestMapper requestMapper;
    private final DtoMapper dtoMapper;

    public MessageService(final TransactionTemplate transactionTemplate,
            final BlacklistService blacklistService,
            final DbIntegration dbIntegration,
            final ContactSettingsIntegration contactSettingsIntegration,
            final SmsSenderIntegration smsSender,
            final EmailSenderIntegration emailSender,
            final DigitalMailSenderIntegration digitalMailSender,
            final WebMessageSenderIntegration webMessageSender,
            final SnailMailSenderIntegration snailmailSender,
            final SlackIntegration slackIntegration,
            final MessageMapper messageMapper,
            final RequestMapper requestMapper,
            final DtoMapper dtoMapper) {
        this.transactionTemplate = transactionTemplate;
        this.blacklistService = blacklistService;
        this.dbIntegration = dbIntegration;
        this.contactSettingsIntegration = contactSettingsIntegration;
        this.smsSender = smsSender;
        this.emailSender = emailSender;
        this.digitalMailSender = digitalMailSender;
        this.webMessageSender = webMessageSender;
        this.snailmailSender = snailmailSender;
        this.slackIntegration = slackIntegration;
        this.messageMapper = messageMapper;
        this.requestMapper = requestMapper;
        this.dtoMapper = dtoMapper;
    }

    public InternalDeliveryResult sendSms(final SmsRequest request) {
        // Check blacklist
        blacklistService.check(request);

        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryResult sendEmail(final EmailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryResult sendWebMessage(final WebMessageRequest request) {
        // Check blacklist
        blacklistService.check(request);

        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryBatchResult sendDigitalMail(final DigitalMailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();
        // Save the message(s)
        var deliveries = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));
        // Deliver them
        var deliveryResults = deliveries.stream()
            .map(this::deliver)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveryResults);
    }

    public InternalDeliveryResult sendDigitalInvoice(final DigitalInvoiceRequest request) {
        // Check blacklist
        blacklistService.check(request);

        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryResult sendSnailMail(final SnailMailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    public InternalDeliveryBatchResult sendMessages(final MessageRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();
        var messages = request.messages().stream()
            .map(message -> messageMapper.toMessage(batchId, message))
            .map(dbIntegration::saveMessage)
            .toList();

        // Handle and send each message individually, since we don't know if it will result in zero,
        // one or more actual deliveries
        var deliveryResults = messages.stream()
            .map(this::sendMessage)
            .flatMap(Collection::stream)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveryResults);
    }

    public InternalDeliveryBatchResult sendLetter(final LetterRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();
        var deliveries = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

        // Handle and send each message individually, since we don't know if it will result in zero,
        // one or more actual deliveries
        var deliveryResults = deliveries.stream()
            .map(this::sendLetter)
            .flatMap(Collection::stream)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveryResults);
    }

    public InternalDeliveryResult sendToSlack(final SlackRequest request) {
        // Check blacklist
        blacklistService.check(request);

        // Save the message and (try to) deliver it
        return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request)));
    }

    List<InternalDeliveryResult> sendMessage(final Message message) {
        var deliveryResults = new ArrayList<InternalDeliveryResult>();

        var partyId = message.partyId();

        // Get the message filters
        var request = fromJson(message.content(), MessageRequest.Message.class);
        var filters = ofNullable(request.filters())
            .map(LinkedMultiValueMap::new)
            .orElseGet(LinkedMultiValueMap::new);

        // Get contact settings and maybe act upon them
        var contactSettings = contactSettingsIntegration.getContactSettings(partyId, filters);
        if (contactSettings.isEmpty()) {
            LOG.info("No contact settings found for {} with filters {}", partyId, filters);

            // No contact settings found - can't do anything more here
            archiveMessage(message.withStatus(NO_CONTACT_SETTINGS_FOUND));

            deliveryResults.add(new InternalDeliveryResult(message, NO_CONTACT_SETTINGS_FOUND));
        } else {
            for (var contactSetting : contactSettings) {
                // Determine the contact method, if any
                var actualContactMethod = ofNullable(contactSetting.contactMethod())
                    .map(contactMethod -> {
                        if (contactSetting.disabled()) {
                            return NO_CONTACT;
                        }

                        return contactMethod;
                    })
                    .orElse(UNKNOWN);

                // Re-map the delivery to use the actual contact method and deliver it
                switch (actualContactMethod) {
                    case EMAIL -> {
                        var deliveryId = UUID.randomUUID().toString();

                        LOG.info("Handling incoming message {} as e-mail with delivery id {}", message.messageId(), deliveryId);

                        var delivery = message
                            .withDeliveryId(deliveryId)
                            .withType(EMAIL)
                            .withContent(requestMapper.toEmailRequest(message, contactSetting.destination()));

                        // Save the re-mapped delivery
                        dbIntegration.saveMessage(delivery);
                        // Delete the original delivery
                        dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                        deliveryResults.add(deliver(delivery));
                    }
                    case SMS -> {
                        var deliveryId = UUID.randomUUID().toString();

                        LOG.info("Handling incoming message {} as SMS with delivery id {}", message.messageId(), deliveryId);

                        var delivery = message
                            .withDeliveryId(deliveryId)
                            .withType(SMS)
                            .withContent(requestMapper.toSmsRequest(message, contactSetting.destination()));

                        // Save the re-mapped delivery
                        dbIntegration.saveMessage(delivery);
                        // Delete the original delivery
                        dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

                        deliveryResults.add(deliver(delivery));
                    }
                    case NO_CONTACT -> {
                        LOG.info("No contact wanted for {} ({}). No delivery will be attempted", partyId, contactSetting.contactMethod());

                        archiveMessage(message.withStatus(NO_CONTACT_WANTED));

                        deliveryResults.add(new InternalDeliveryResult(message, NO_CONTACT_WANTED));
                    }
                    default -> {
                        LOG.warn("Unknown/missing contact method for message {} and delivery id {} - will not be delivered",
                            message.messageId(), message.deliveryId());

                        var statusDetail = String.format(
                            "Unknown/missing contact method for message %s and delivery id %s",
                            message.messageId(), message.deliveryId());

                        archiveMessage(message.withStatus(FAILED), statusDetail);

                        deliveryResults.add(new InternalDeliveryResult(message, FAILED));
                    }
                }
            }
        }

        return deliveryResults;
    }

    public List<InternalDeliveryResult> sendLetter(final Message message) {
        var result = new ArrayList<InternalDeliveryResult>();
        var request = fromJson(message.content(), LetterRequest.class);

        // Re-map the request as a digital mail request
        var digitalMailRequest = requestMapper.toDigitalMailRequest(request, message.partyId());
        var digitalMailRequestAsJson = toJson(digitalMailRequest);

        // Re-map the request as a snail-mail request
        var snailMailRequest = requestMapper.toSnailMailRequest(request, message.partyId());
        var snailMailRequestAsJson = toJson(snailMailRequest);

        // Don't make an attempt to deliver as digital mail if there aren't any attachments
        // intended for it
        if (!digitalMailRequest.attachments().isEmpty()) {
            // "Re-route" the message as digital mail
            var reroutedMessage = dbIntegration.saveMessage(message
                .withType(DIGITAL_MAIL)
                .withContent(digitalMailRequestAsJson));

            try {
                // Deliver it
                result.add(deliver(reroutedMessage));

                return result;
            } catch (Exception e) {
                LOG.info("Unable to send LETTER as DIGITAL_MAIL");

                result.add(new InternalDeliveryResult(reroutedMessage.withStatus(FAILED)));
            }
        } else {
            // No attachments intended for digital mail
            LOG.info("No attachment(s) for DIGITAL_MAIL - switching over to snail-mail");
        }

        // Don't make an attempt to deliver as snail mail if there aren't any attachments
        // intended for it
        if (!snailMailRequest.attachments().isEmpty()) {
            // Re-route the message as snail-mail
            var reroutedMessage = dbIntegration.saveMessage(message
                .withDeliveryId(UUID.randomUUID().toString())
                .withType(SNAIL_MAIL)
                .withContent(snailMailRequestAsJson));

            try {
                // Deliver it
                result.add(deliver(reroutedMessage));
            } catch (Exception e) {
                LOG.info("Unable to send LETTER as SNAIL_MAIL");

                result.add(new InternalDeliveryResult(reroutedMessage.withStatus(FAILED)));
            }
        } else {
            LOG.info("No attachment(s) for SNAIL_MAIL - unable to send letter");
        }

        return result;
    }

    InternalDeliveryResult deliver(final Message delivery) {
        // Re-construct the original request
        var request = fromJson(delivery.content(), switch (delivery.type()) {
            case SMS -> SmsRequest.class;
            case EMAIL -> EmailRequest.class;
            case DIGITAL_MAIL -> DigitalMailRequest.class;
            case DIGITAL_INVOICE -> DigitalInvoiceRequest.class;
            case WEB_MESSAGE -> WebMessageRequest.class;
            case SNAIL_MAIL -> SnailMailRequest.class;
            case SLACK -> SlackRequest.class;
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
            case DIGITAL_INVOICE -> ofCallable(() ->
                digitalMailSender.sendDigitalInvoice(dtoMapper.toDigitalInvoiceDto((DigitalInvoiceRequest) request)));
            case WEB_MESSAGE -> ofCallable(() ->
                webMessageSender.sendWebMessage(dtoMapper.toWebMessageDto((WebMessageRequest) request)));
            case SNAIL_MAIL -> ofCallable(() ->
                snailmailSender.sendSnailMail(dtoMapper.toSnailMailDto((SnailMailRequest) request)));
            case SLACK -> ofCallable(() ->
                slackIntegration.sendMessage(dtoMapper.toSlackDto((SlackRequest) request)));
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
                Case($(instanceOf(ThrowableProblem.class)), throwableProblem -> {
                    LOG.info("Unable to deliver {}: {}", delivery.type(), throwableProblem.getMessage());

                    return throwableProblem;
                }),
                Case($(), e -> {
                    LOG.info("Unable to deliver {}: {}", delivery.type(), e.getMessage());

                    return Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to deliver " + delivery.type());
                })
            )
            .toEither()
            // If we have a left (i.e. an exception has occurred) - archive the message and throw
            .peekLeft(throwable -> {
                // Archive the message
                archiveMessage(delivery.withStatus(FAILED), throwable.getMessage());

                throw (ThrowableProblem) throwable;
            })
            .get();
    }

    void archiveMessage(final Message message) {
        archiveMessage(message, null);
    }

    void archiveMessage(final Message message, final String statusDetail) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NotNull final TransactionStatus ignored) {
                LOG.debug("Moving {} delivery {} with status {} to history", message.type(),
                    message.deliveryId(), message.status());

                dbIntegration.saveHistory(message, statusDetail);
                dbIntegration.deleteMessageByDeliveryId(message.deliveryId());
            }
        });
    }
}
