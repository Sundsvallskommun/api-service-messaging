package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
class DigitalMailRequestTests {

    @Test
    void testConstructorAndGetters() {
        // Party
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new DigitalMailRequest.Party(List.of("somePartyId"), externalReferences);
        // Headers
        var headers = List.of(new Header(HeaderName.TYPE, List.of("someValue", "anotherValue")));
        // Sender
        var supportInfo = new DigitalMailRequest.Sender.SupportInfo("someText", "someEmailAddress",
            "somePhoneNumber", "someUrl");
        var sender = new DigitalMailRequest.Sender(supportInfo);
        // Attachments
        var attachments = List.of(new DigitalMailRequest.Attachment("someContentType",
            "someContent", "someFilename"));

        var request = new DigitalMailRequest(party, headers, sender, "someSubject", "HTML", "someBody", attachments);

        assertThat(request.party()).satisfies(requestParty -> {
            assertThat(requestParty.partyIds()).containsOnly("somePartyId");
            assertThat(requestParty.externalReferences()).hasSize(1);
        });
        assertThat(request.headers()).hasSize(1);
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
    }
}
