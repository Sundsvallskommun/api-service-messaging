package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SnailMailRequestTest {

	@Test
	void testConstructorAndGetters() {
		final var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
		final var party = new SnailMailRequest.Party("somePartyId", externalReferences);
		final var attachments = List.of(new SnailMailRequest.Attachment("someName", "someContentType", "someContent"));
		final var request = new SnailMailRequest(party, "someDepartment", "someDeviation", "someOrigin", attachments);

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.party()).satisfies(requestParty -> {
			assertThat(requestParty.partyId()).isEqualTo("somePartyId");
			assertThat(requestParty.externalReferences()).hasSize(1).element(0).satisfies(extRef -> {
				assertThat(extRef.key()).isEqualTo("someKey");
				assertThat(extRef.value()).isEqualTo("someValue");
			});
		});
		assertThat(request.department()).isEqualTo("someDepartment");
		assertThat(request.deviation()).isEqualTo("someDeviation");
		assertThat(request.origin()).isEqualTo("someOrigin");
		assertThat(request.attachments()).hasSize(1).element(0).satisfies(attachment -> {
			assertThat(attachment.name()).isEqualTo("someName");
			assertThat(attachment.contentType()).isEqualTo("someContentType");
			assertThat(attachment.content()).isEqualTo("someContent");
		});
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SnailMailRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
