package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

class MessageRequestTests {

    @Test
    void testBuilderAndGetters() {
        var partyId = UUID.randomUUID().toString();

        var message = MessageRequest.Message.builder()
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withMessage("message")
            .withSubject("subject")
            .withSenderEmail("noreply@sundsvall.se")
            .withSmsName("Sundsvall")
            .withEmailName("Sundsvalls kommun")
            .build();

        var request = MessageRequest.builder()
            .withMessages(List.of(message))
            .build();

        assertThat(request.getMessages()).hasSize(1).allSatisfy(msg -> {
            assertThat(msg.getParty().getPartyId()).isEqualTo(partyId);
            assertThat(msg.getParty().getExternalReferences()).hasSize(1);
            assertThat(msg.getMessage()).isEqualTo("message");
            assertThat(msg.getSubject()).isEqualTo("subject");
            assertThat(msg.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
            assertThat(msg.getSmsName()).isEqualTo("Sundsvall");
            assertThat(msg.getEmailName()).isEqualTo("Sundsvalls kommun");
        });
    }

    @Test
    void testSetters() {
        var partyId = UUID.randomUUID().toString();

        var party = new Party();
        party.setPartyId(partyId);
        party.setExternalReferences(List.of(ExternalReference.builder().build()));

        var message = new MessageRequest.Message();
        message.setParty(party);
        message.setMessage("message");
        message.setSubject("subject");
        message.setSenderEmail("noreply@sundsvall.se");
        message.setSmsName("Sundsvall");
        message.setEmailName("Sundsvalls kommun");

        var request = new MessageRequest();
        request.setMessages(List.of(message));

        assertThat(request.getMessages()).hasSize(1).allSatisfy(msg -> {
            assertThat(msg.getParty().getPartyId()).isEqualTo(partyId);
            assertThat(msg.getParty().getExternalReferences()).hasSize(1);
            assertThat(msg.getMessage()).isEqualTo("message");
            assertThat(msg.getSubject()).isEqualTo("subject");
            assertThat(msg.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
            assertThat(msg.getSmsName()).isEqualTo("Sundsvall");
            assertThat(msg.getEmailName()).isEqualTo("Sundsvalls kommun");
        });
    }
}
