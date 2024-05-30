package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsBatchRequestTests {

    @Test
    void testConstructorAndGetters() {
		var party = new SmsBatchRequest.Party("somePartyId", "someMobileNumber");
		var request = new SmsBatchRequest("someSender", "someOrigin", "someMessage", List.of(party));

		assertThat(request.parties()).isNotNull().hasSize(1).allSatisfy(requestParty -> {
            assertThat(requestParty.partyId()).isEqualTo("somePartyId");
			assertThat(requestParty.mobileNumber()).isEqualTo("someMobileNumber");
        });
        assertThat(request.sender()).isEqualTo("someSender");
        assertThat(request.message()).isEqualTo("someMessage");
        assertThat(request.origin()).isEqualTo("someOrigin");
    }
}
