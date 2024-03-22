package se.sundsvall.messaging.api.model.request;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class DigitalMailRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new DigitalMailRequest.Party(List.of("somePartyId"), externalReferences);
        var supportInfo = new DigitalMailRequest.Sender.SupportInfo("someText", "someEmailAddress",
            "somePhoneNumber", "someUrl");
        var sender = new DigitalMailRequest.Sender(supportInfo);
        var attachments = List.of(new DigitalMailRequest.Attachment("someContentType",
            "someContent", "someFilename"));

        var request = new DigitalMailRequest(party, sender, "someSubject", "someDepartment", "HTML", "someBody", "someOrigin", attachments);

        assertThat(request.party()).satisfies(requestParty -> {
            assertThat(requestParty.partyIds()).containsOnly("somePartyId");
            assertThat(requestParty.externalReferences()).hasSize(1);
        });
        assertThat(request.sender())
            .extracting(DigitalMailRequest.Sender::supportInfo)
            .satisfies(requestSupportInfo -> {
                assertThat(requestSupportInfo.text()).isEqualTo("someText");
                assertThat(requestSupportInfo.emailAddress()).isEqualTo("someEmailAddress");
                assertThat(requestSupportInfo.phoneNumber()).isEqualTo("somePhoneNumber");
                assertThat(requestSupportInfo.url()).isEqualTo("someUrl");
            });
        assertThat(request.attachments())
            .hasSize(1)
            .allSatisfy(requestAttachment -> {
                assertThat(requestAttachment.contentType()).isEqualTo("someContentType");
                assertThat(requestAttachment.content()).isEqualTo("someContent");
                assertThat(requestAttachment.filename()).isEqualTo("someFilename");
            });
        assertThat(request.subject()).isEqualTo("someSubject");
        assertThat(request.department()).isEqualTo("someDepartment");
        assertThat(request.origin()).isEqualTo("someOrigin");
    }
}
