package se.sundsvall.messaging.api.model.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MailboxTest {

	@Test
	void testBuilderAndGetters() {
		final var partyId = "partyId";
		final var supplier = "Kivra";
		final var reachable = true;
		final var reason = "Some reason";

		final var mailbox = Mailbox.builder()
			.withPartyId(partyId)
			.withSupplier(supplier)
			.withReachable(reachable)
			.withReason(reason)
			.build();

		assertThat(mailbox.partyId()).isEqualTo(partyId);
		assertThat(mailbox.supplier()).isEqualTo(supplier);
		assertThat(mailbox.reachable()).isTrue();
		assertThat(mailbox.reason()).isEqualTo(reason);

		assertThat(mailbox).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Mailbox.builder().build()).hasAllNullFieldsOrPropertiesExcept("reachable");
	}
}
