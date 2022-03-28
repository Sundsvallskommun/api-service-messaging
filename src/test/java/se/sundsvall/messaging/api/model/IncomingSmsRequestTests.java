package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

class IncomingSmsRequestTests {

    @Test
    void testBuilderAndGetters() {
        var partyId = UUID.randomUUID().toString();

        var request = SmsRequest.builder()
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withMessage("message")
            .withMobileNumber("+46701234567")
            .withSender("Sundsvall")
            .build();

        assertThat(request.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(request.getParty().getExternalReferences()).hasSize(1);
        assertThat(request.getMessage()).isEqualTo("message");
        assertThat(request.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(request.getSender()).isEqualTo("Sundsvall");
    }

    @Test
    void testSetters() {
        var partyId = UUID.randomUUID().toString();

        var party = new Party();
        party.setPartyId(partyId);
        party.setExternalReferences(List.of(new ExternalReference()));

        var request = new SmsRequest();
        request.setParty(party);
        request.setMessage("message");
        request.setMobileNumber("+46701234567");
        request.setSender("Sundsvall");

        assertThat(request.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(request.getParty().getExternalReferences()).hasSize(1);
        assertThat(request.getMessage()).isEqualTo("message");
        assertThat(request.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(request.getSender()).isEqualTo("Sundsvall");
    }
}
