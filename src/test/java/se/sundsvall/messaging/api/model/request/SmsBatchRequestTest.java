package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsBatchRequestTest {

	@Test
	void testConstructorAndGetters() {
		final var party = new SmsBatchRequest.Party("somePartyId", "someMobileNumber");
		final var request = new SmsBatchRequest("someSender", "someOrigin", "someMessage", Priority.HIGH, List.of(party));

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.parties()).isNotNull().hasSize(1).allSatisfy(requestParty -> {
			assertThat(requestParty.partyId()).isEqualTo("somePartyId");
			assertThat(requestParty.mobileNumber()).isEqualTo("someMobileNumber");
		});
		assertThat(request.sender()).isEqualTo("someSender");
		assertThat(request.message()).isEqualTo("someMessage");
		assertThat(request.origin()).isEqualTo("someOrigin");
		assertThat(request.priority()).isEqualTo(Priority.HIGH);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SmsBatchRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
