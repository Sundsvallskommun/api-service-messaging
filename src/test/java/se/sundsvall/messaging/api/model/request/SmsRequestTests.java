package se.sundsvall.messaging.api.model.request;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class SmsRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new SmsRequest.Party("somePartyId", externalReferences);
        var request = new SmsRequest(party, "someSender", "someMobileNumber", "someOrigin", "someMessage");

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
    }
}
