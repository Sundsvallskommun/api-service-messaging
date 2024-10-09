package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.api.model.request.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.Header.REFERENCES;

import org.junit.jupiter.api.Test;

class HeaderTest {

	@Test
	void testEnumValues() {
		assertThat(Header.values()).containsExactlyInAnyOrder(
			IN_REPLY_TO,
			MESSAGE_ID,
			REFERENCES);
	}

}
