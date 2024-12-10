package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.ReferenceType.SE_OCR;
import static se.sundsvall.messaging.model.ReferenceType.TENANT_REF;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class ReferenceTypeTest {

	@Test
	void testEnumValues() {
		assertThat(ReferenceType.values()).containsExactlyInAnyOrder(
			SE_OCR,
			TENANT_REF);
	}
}
