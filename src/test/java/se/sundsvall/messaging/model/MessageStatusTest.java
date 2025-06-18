package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.AWAITING_FEEDBACK;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import org.junit.jupiter.api.Test;

class MessageStatusTest {

	@Test
	void testEnumValues() {
		assertThat(MessageStatus.values()).containsExactlyInAnyOrder(
			AWAITING_FEEDBACK,
			FAILED,
			NO_CONTACT_SETTINGS_FOUND,
			NO_CONTACT_WANTED,
			NOT_SENT,
			PENDING,
			SENT);
	}
}
