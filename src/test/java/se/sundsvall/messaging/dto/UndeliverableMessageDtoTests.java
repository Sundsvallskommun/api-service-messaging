package se.sundsvall.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

class UndeliverableMessageDtoTests {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();

        var undeliverableMessageDto = UndeliverableMessageDto.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withSenderEmail("noreply@sundsvall.se")
            .withSenderName("Sundsvalls kommun")
            .withPartyContact("john.doe@example.com")
            .withSubject("subject")
            .withContent("message")
            .withType(MessageType.EMAIL)
            .withStatus(MessageStatus.SENT)
            .build();

        assertThat(undeliverableMessageDto.getBatchId()).isEqualTo(batchId);
        assertThat(undeliverableMessageDto.getMessageId()).isEqualTo(messageId);
        assertThat(undeliverableMessageDto.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(undeliverableMessageDto.getParty().getExternalReferences()).hasSize(1);
        assertThat(undeliverableMessageDto.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(undeliverableMessageDto.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(undeliverableMessageDto.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(undeliverableMessageDto.getSubject()).isEqualTo("subject");
        assertThat(undeliverableMessageDto.getContent()).isEqualTo("message");
        assertThat(undeliverableMessageDto.getType()).isEqualTo(MessageType.EMAIL);
        assertThat(undeliverableMessageDto.getStatus()).isEqualTo(MessageStatus.SENT);
    }
}
