package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsRequestTests {

	@Test
	void testConstructorAndGetters() {
		final var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
		final var party = new SmsRequest.Party("somePartyId", externalReferences);
		final var request = new SmsRequest(party, "someSender", "someMobileNumber", "someOrigin", "someMessage", Priority.HIGH);

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.party()).satisfies(requestParty -> {
			assertThat(requestParty.partyId()).isEqualTo("somePartyId");
			assertThat(requestParty.externalReferences()).hasSize(1).element(0).satisfies(extRef -> {
				assertThat(extRef.key()).isEqualTo("someKey");
				assertThat(extRef.value()).isEqualTo("someValue");
			});
		});
		assertThat(request.sender()).isEqualTo("someSender");
		assertThat(request.mobileNumber()).isEqualTo("someMobileNumber");
		assertThat(request.message()).isEqualTo("someMessage");
		assertThat(request.origin()).isEqualTo("someOrigin");
		assertThat(request.priority()).isEqualTo(Priority.HIGH);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SmsRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
