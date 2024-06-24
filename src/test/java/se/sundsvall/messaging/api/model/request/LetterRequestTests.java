package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class LetterRequestTests {

	@Test
	void testConstructorAndGetters() {
		final var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
		final var party = new LetterRequest.Party(List.of("somePartyId"), externalReferences);
		final var supportInfo = new LetterRequest.Sender.SupportInfo("someText", "someEmailAddress",
			"somePhoneNumber", "someUrl");
		final var sender = new LetterRequest.Sender(supportInfo);
		final var attachments = List.of(new LetterRequest.Attachment(LetterRequest.Attachment.DeliveryMode.ANY,
			"someFilename", "someContentType", "someContent"));

		final var request = new LetterRequest(party, "someSubject", sender, "someContentType", "someBody", "someDepartment", "someDeviation", "someOrigin", attachments);

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.party()).satisfies(requestParty -> {
			assertThat(requestParty.partyIds()).containsOnly("somePartyId");
			assertThat(requestParty.externalReferences()).hasSize(1);
		});
		assertThat(request.sender())
			.extracting(LetterRequest.Sender::supportInfo)
			.satisfies(requestSupportInfo -> {
				assertThat(requestSupportInfo.text()).isEqualTo("someText");
				assertThat(requestSupportInfo.emailAddress()).isEqualTo("someEmailAddress");
				assertThat(requestSupportInfo.phoneNumber()).isEqualTo("somePhoneNumber");
				assertThat(requestSupportInfo.url()).isEqualTo("someUrl");
			});
		assertThat(request.subject()).isEqualTo("someSubject");
		assertThat(request.contentType()).isEqualTo("someContentType");
		assertThat(request.body()).isEqualTo("someBody");
		assertThat(request.department()).isEqualTo("someDepartment");
		assertThat(request.deviation()).isEqualTo("someDeviation");
		assertThat(request.attachments())
			.hasSize(1)
			.allSatisfy(requestAttachment -> {
				assertThat(requestAttachment.deliveryMode()).isEqualTo(LetterRequest.Attachment.DeliveryMode.ANY);
				assertThat(requestAttachment.isIntendedForSnailMail()).isTrue();
				assertThat(requestAttachment.isIntendedForDigitalMail()).isTrue();
				assertThat(requestAttachment.contentType()).isEqualTo("someContentType");
				assertThat(requestAttachment.content()).isEqualTo("someContent");
				assertThat(requestAttachment.filename()).isEqualTo("someFilename");
			});
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(LetterRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
