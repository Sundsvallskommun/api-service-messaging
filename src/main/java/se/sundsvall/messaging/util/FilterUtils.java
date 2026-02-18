package se.sundsvall.messaging.util;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import se.sundsvall.messaging.api.model.response.UserMessage;

import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

public class FilterUtils {
	private FilterUtils() {
		// To prevent instantiation
	}

	public static boolean isDigitalMailAndUnsuccessful(UserMessage.Recipient recipient) {
		return Objects.equals(DIGITAL_MAIL.name(), recipient.messageType()) &&
			ObjectUtils.notEqual(SENT.name(), recipient.status());
	}

	public static boolean isSnailMailSuccessful(String personId, List<UserMessage.Recipient> recipients) {
		return recipients.stream()
			.filter(recipient -> Objects.equals(SNAIL_MAIL.name(), recipient.messageType()))
			.filter(recipient -> Objects.nonNull(recipient.personId()))
			.filter(recipient -> Objects.equals(personId, recipient.personId()))
			.anyMatch(recipient -> Objects.equals(SENT.name(), recipient.status())); // If snail mail has been successfully sent, the digital mail entry should be filtered out
	}
}
