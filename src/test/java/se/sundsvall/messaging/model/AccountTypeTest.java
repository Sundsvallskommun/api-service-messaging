package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.AccountType.BANKGIRO;
import static se.sundsvall.messaging.model.AccountType.PLUSGIRO;

import org.junit.jupiter.api.Test;

class AccountTypeTest {

	@Test
	void testEnumValues() {
		assertThat(AccountType.values()).containsExactlyInAnyOrder(
			BANKGIRO,
			PLUSGIRO);
	}
}
