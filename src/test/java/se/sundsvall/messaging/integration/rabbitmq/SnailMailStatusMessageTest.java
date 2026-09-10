package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SnailMailStatusMessageTest {

	@Test
	void isTheContractPostportalConsumes() {
		// The field names are the wire contract with postportal and are not free to rename: the outcome is read from
		// the payload rather than from the routing key, since dead-lettering rewrites the latter.
		final var message = new SnailMailStatusMessage("recipient-id", "SENT", "external-id", "detail");

		assertThat(message.recipientId()).isEqualTo("recipient-id");
		assertThat(message.status()).isEqualTo("SENT");
		assertThat(message.externalId()).isEqualTo("external-id");
		assertThat(message.statusDetail()).isEqualTo("detail");
		assertThat(message).hasNoNullFieldsOrProperties();
	}

	@Test
	void carriesNoExternalIdOnAFailure() {
		final var message = new SnailMailStatusMessage("recipient-id", "FAILED", null, "attempts exhausted");

		assertThat(message.externalId()).isNull();
		assertThat(message.statusDetail()).isEqualTo("attempts exhausted");
	}
}
