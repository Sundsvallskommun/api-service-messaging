package se.sundsvall.messaging.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_INVOICE;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

class MessageTypeTest {

	@Test
	void testEnumValues() {
		assertThat(MessageType.values()).containsExactlyInAnyOrder(
			DIGITAL_INVOICE,
			DIGITAL_MAIL,
			EMAIL,
			LETTER,
			MESSAGE,
			SLACK,
			SMS,
			SNAIL_MAIL,
			WEB_MESSAGE);
	}
}
