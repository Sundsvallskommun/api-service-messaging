package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PriorityTest {

	@Test
	void testEnumValues() {
		assertThat(Priority.values()).containsExactlyInAnyOrder(
			Priority.HIGH,
			Priority.NORMAL);
	}

}
