package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
class EmailRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new EmailRequest.Party("somePartyId", externalReferences);
        var headers = List.of(new Header(HeaderName.TYPE, List.of("someValue", "anotherValue")));
        var sender = new EmailRequest.Sender("someName", "someAddress", "someReplyTo");
        var attachments = List.of(new EmailRequest.Attachment("someName", "someContentType", "someContent"));
        var request = new EmailRequest(party, headers, "someEmailAddress", "someSubject",
            "someMessage", "someHtmlMessage", sender, attachments);

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
        assertThat(request.sender()).satisfies(requestSender -> {
            assertThat(requestSender.name()).isEqualTo("someName");
            assertThat(requestSender.address()).isEqualTo("someAddress");
            assertThat(requestSender.replyTo()).isEqualTo("someReplyTo");
        });
        assertThat(request.message()).isEqualTo("someMessage");
        assertThat(request.htmlMessage()).isEqualTo("someHtmlMessage");
        assertThat(request.attachments()).hasSize(1).element(0).satisfies(attachment -> {
            assertThat(attachment.name()).isEqualTo("someName");
            assertThat(attachment.contentType()).isEqualTo("someContentType");
            assertThat(attachment.content()).isEqualTo("someContent");
        });
    }
}
