package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
class SmsRequestTests {

    @Test
    void testConstructorAndGetters() {
        var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
        var party = new SmsRequest.Party("somePartyId", externalReferences);
        var headers = List.of(new Header(HeaderName.TYPE, List.of("someValue", "anotherValue")));
        var request = new SmsRequest(party, headers, "someSender", "someMobileNumber", "someMessage");

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
        assertThat(request.sender()).isEqualTo("someSender");
        assertThat(request.mobileNumber()).isEqualTo("someMobileNumber");
        assertThat(request.message()).isEqualTo("someMessage");
    }
}
