package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class WebMessageRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new WebMessageRequest.Party("somePartyId", externalReferences);
        var attachments = List.of(new WebMessageRequest.Attachment("someName", "someMimeType", "someBase64Data"));
        var request = new WebMessageRequest(party, "someMessage", attachments);

        assertThat(request.party()).satisfies(requestParty -> {
            assertThat(requestParty.partyId()).isEqualTo("somePartyId");
            assertThat(requestParty.externalReferences()).hasSize(1).element(0).satisfies(extRef -> {
                assertThat(extRef.key()).isEqualTo("someKey");
                assertThat(extRef.value()).isEqualTo("someValue");
            });
        });
        assertThat(request.message()).isEqualTo("someMessage");
        assertThat(request.attachments()).hasSize(1).element(0).satisfies(attachment -> {
            assertThat(attachment.fileName()).isEqualTo("someName");
            assertThat(attachment.mimeType()).isEqualTo("someMimeType");
            assertThat(attachment.base64Data()).isEqualTo("someBase64Data");
        });
    }
}
