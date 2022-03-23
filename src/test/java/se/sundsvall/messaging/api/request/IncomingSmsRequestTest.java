package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class IncomingSmsRequestTest {

    @Test
    void testBuilderAndGetters() {
        String partyId = UUID.randomUUID().toString();

        IncomingSmsRequest sms = IncomingSmsRequest.builder()
                .withPartyId(partyId)
                .withMessage("message")
                .withMobileNumber("+46701234567")
                .withSender("Sundsvall")
                .build();

        assertThat(sms.getPartyId()).isEqualTo(partyId);
        assertThat(sms.getMessage()).isEqualTo("message");
        assertThat(sms.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(sms.getSender()).isEqualTo("Sundsvall");
    }

    @Test
    void testSetters() {
        String partyId = UUID.randomUUID().toString();

        IncomingSmsRequest sms = new IncomingSmsRequest();
        sms.setPartyId(partyId);
        sms.setMessage("message");
        sms.setMobileNumber("+46701234567");
        sms.setSender("Sundsvall");

        assertThat(sms.getPartyId()).isEqualTo(partyId);
        assertThat(sms.getMessage()).isEqualTo("message");
        assertThat(sms.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(sms.getSender()).isEqualTo("Sundsvall");
        // Just for coverage
        assertThat(sms.toString()).isNotNull();
    }

}
