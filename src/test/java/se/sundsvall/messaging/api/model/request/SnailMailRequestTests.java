package se.sundsvall.messaging.api.model.request;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class SnailMailRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new SnailMailRequest.Party("somePartyId", externalReferences);
        var attachments = List.of(new SnailMailRequest.Attachment("someName", "someContentType", "someContent"));
        var request = new SnailMailRequest(party, "someDepartment", "someDeviation", "someOrigin", attachments);

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
}
