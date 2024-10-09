package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.api.model.request.Priority.HIGH;
import static se.sundsvall.messaging.api.model.request.Priority.NORMAL;

import org.junit.jupiter.api.Test;

class PriorityTest {

	@Test
	void testEnumValues() {
		assertThat(Priority.values()).containsExactlyInAnyOrder(
			HIGH,
			NORMAL);
	}
}
