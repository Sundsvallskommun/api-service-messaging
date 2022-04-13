package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;

import org.junit.jupiter.api.Test;

class SmsRequestTests {

    @Test
    void testBuilderAndGetters() {
        var request = createSmsRequest();

        assertThat(request.getParty()).satisfies(party -> {
            assertThat(party.getPartyId()).isEqualTo("somePartyId");
            assertThat(party.getExternalReferences()).hasSize(1).allSatisfy(externalReference -> {
                assertThat(externalReference.getKey()).isEqualTo("someKey");
                assertThat(externalReference.getValue()).isEqualTo("someValue");
            });
        });
        assertThat(request.getHeaders()).satisfies(headers ->
            assertThat(headers).hasSize(1).allSatisfy(header -> {
                assertThat(header.getName()).isEqualTo("someName");
                assertThat(header.getValues()).containsExactlyInAnyOrder("someValue1", "someValue2");
            })
        );
        assertThat(request.getSender().getName()).isEqualTo("someSender");
        assertThat(request.getMobileNumber()).isEqualTo("someMobileNumber");
        assertThat(request.getMessage()).isEqualTo("someMessage");
    }
}
