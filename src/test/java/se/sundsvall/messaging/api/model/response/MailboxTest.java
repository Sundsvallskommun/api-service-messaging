package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MailboxTest {

	@Test
	void testBuilderAndGetters() {
		final var partyId = "partyId";
		final var supplier = "Kivra";
		final var reachable = true;

		final var mailbox = Mailbox.builder()
			.withPartyId(partyId)
			.withSupplier(supplier)
			.withReachable(reachable)
			.build();

		assertThat(mailbox.partyId()).isEqualTo(partyId);
		assertThat(mailbox.supplier()).isEqualTo(supplier);
		assertThat(mailbox.reachable()).isTrue();

		assertThat(mailbox).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Mailbox.builder().build()).hasAllNullFieldsOrPropertiesExcept("reachable");
	}
}
