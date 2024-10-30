package se.sundsvall.messaging.service;

import static org.apache.commons.collections4.MapUtils.isEmpty;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.configuration.BlacklistProperties;
import se.sundsvall.messaging.model.MessageType;

@Service
public class BlacklistService {

	private static final Logger LOG = LoggerFactory.getLogger(BlacklistService.class);

	private final BlacklistProperties properties;

	public BlacklistService(final BlacklistProperties properties) {
		this.properties = properties;

		if (properties.enabled()) {
			LOG.info("Blacklist is ENABLED");
		} else {
			LOG.info("Blacklist is NOT ENABLED");
		}
	}

	public void check(final SmsRequest request) {
		check(SMS, request.mobileNumber());
	}

	public void check(final EmailRequest request) {
		check(EMAIL, request.emailAddress());
	}

	public void check(final DigitalMailRequest request) {
		request.party().partyIds().forEach(partyId -> check(DIGITAL_MAIL, partyId));
	}

	public void check(final DigitalInvoiceRequest request) {
		check(EMAIL, request.party().partyId());
	}

	public void check(final MessageRequest request) {
		request.messages().stream()
			.map(MessageRequest.Message::party)
			.map(MessageRequest.Message.Party::partyId)
			.forEach(partyId -> check(MESSAGE, partyId));
	}

	public void check(final WebMessageRequest request) {
		check(WEB_MESSAGE, request.party().partyId());
	}

	public void check(final LetterRequest request) {
		request.party().partyIds().forEach(partyId -> check(LETTER, partyId));
	}

	public void check(final SlackRequest request) {
		check(SLACK, request.channel());
	}

	public void check(final MessageType messageType, final String value) {
		if (!properties.enabled() || isEmpty(properties.blockedRecipients())) {
			LOG.debug("Blacklist is NOT ENABLED, or no blocked recipients have been defined");

			return;
		}

		if (properties.blockedRecipients().getOrDefault(messageType, List.of()).contains(value)) {
			throw Problem.valueOf(Status.BAD_REQUEST, "%s is blacklisted for %s".formatted(value, messageType));
		}
	}
}
