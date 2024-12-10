package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.InvoiceType.INVOICE;
import static se.sundsvall.messaging.model.InvoiceType.REMINDER;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class InvoiceTypeTest {

	@Test
	void testEnumValues() {
		assertThat(InvoiceType.values()).containsExactlyInAnyOrder(
			INVOICE,
			REMINDER);
	}
}
