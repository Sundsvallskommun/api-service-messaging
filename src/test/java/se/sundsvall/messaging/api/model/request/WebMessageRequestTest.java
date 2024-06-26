package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class WebMessageRequestTest {

	@Test
	void testConstructorAndGetters() {
		final var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
		final var party = new WebMessageRequest.Party("somePartyId", externalReferences);
		final var attachments = List.of(new WebMessageRequest.Attachment("someName", "someMimeType", "someBase64Data"));
		final var oepInstance = "internal";
		final var request = new WebMessageRequest(party, "someMessage", "someOrigin", oepInstance, attachments);

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.party()).satisfies(requestParty -> {
			assertThat(requestParty.partyId()).isEqualTo("somePartyId");
			assertThat(requestParty.externalReferences()).hasSize(1).element(0).satisfies(extRef -> {
				assertThat(extRef.key()).isEqualTo("someKey");
				assertThat(extRef.value()).isEqualTo("someValue");
			});
		});
		assertThat(request.oepInstance()).isEqualTo(oepInstance);
		assertThat(request.message()).isEqualTo("someMessage");
		assertThat(request.origin()).isEqualTo("someOrigin");
		assertThat(request.attachments()).hasSize(1).element(0).satisfies(attachment -> {
			assertThat(attachment.fileName()).isEqualTo("someName");
			assertThat(attachment.mimeType()).isEqualTo("someMimeType");
			assertThat(attachment.base64Data()).isEqualTo("someBase64Data");
		});
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(WebMessageRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
