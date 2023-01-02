package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
class WebMessageRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new WebMessageRequest.Party("somePartyId", externalReferences);
        var headers = List.of(new Header(HeaderName.TYPE, List.of("someValue", "anotherValue")));
        var attachments = List.of(new WebMessageRequest.Attachment("someName", "someMimeType", "someBase64Data"));
        var request = new WebMessageRequest(party, headers, "someMessage", attachments);

        assertThat(request.party()).satisfies(requestParty -> {
            assertThat(requestParty.partyId()).isEqualTo("somePartyId");
            assertThat(requestParty.externalReferences()).hasSize(1).element(0).satisfies(extRef -> {
                assertThat(extRef.key()).isEqualTo("someKey");
                assertThat(extRef.value()).isEqualTo("someValue");
            });
        });
        assertThat(request.headers()).hasSize(1).element(0).satisfies(header -> {
            assertThat(header.name()).isEqualTo(HeaderName.TYPE);
            assertThat(header.values()).containsExactlyInAnyOrder("someValue", "anotherValue");
        });
        assertThat(request.message()).isEqualTo("someMessage");
        assertThat(request.attachments()).hasSize(1).element(0).satisfies(attachment -> {
            assertThat(attachment.fileName()).isEqualTo("someName");
            assertThat(attachment.mimeType()).isEqualTo("someMimeType");
            assertThat(attachment.base64Data()).isEqualTo("someBase64Data");
        });
    }
}
