package se.sundsvall.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

class MesageBatchDtoTests {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();

        var message = MessageBatchDto.Message.builder()
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withMessageId(messageId)
            .withEmailName("Sundsvalls kommun")
            .withSmsName("Sundsvall")
            .withSenderEmail("noreply@sundsvall.se")
            .withSubject("subject")
            .withMessage("message")
            .build();

        var messageBatch = MessageBatchDto.builder()
            .withBatchId(batchId)
            .withMessages(List.of(message))
            .build();

        assertThat(messageBatch.getBatchId()).isEqualTo(batchId);
        assertThat(messageBatch.getMessages()).hasSize(1)
            .allSatisfy(msg -> {
                assertThat(msg.getParty().getPartyId()).isEqualTo(partyId);
                assertThat(msg.getParty().getExternalReferences())
                    .hasSameSizeAs(message.getParty().getExternalReferences());
                assertThat(msg.getMessageId()).isEqualTo(messageId);
                assertThat(msg.getEmailName()).isEqualTo("Sundsvalls kommun");
                assertThat(msg.getSmsName()).isEqualTo("Sundsvall");
                assertThat(msg.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
                assertThat(msg.getSubject()).isEqualTo("subject");
                assertThat(msg.getMessage()).isEqualTo("message");
            });
    }
}
