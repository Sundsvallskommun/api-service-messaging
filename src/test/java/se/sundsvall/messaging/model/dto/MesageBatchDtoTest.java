package se.sundsvall.messaging.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MesageBatchDtoTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();

        MessageBatchDto.Message batchMessage = MessageBatchDto.Message.builder()
                .withPartyId(partyId)
                .withMessageId(messageId)
                .withEmailName("Sundsvalls kommun")
                .withSmsName("Sundsvall")
                .withSenderEmail("noreply@sundsvall.se")
                .withSubject("subject")
                .withMessage("message")
                .build();

        MessageBatchDto messageBatch = MessageBatchDto.builder()
                .withBatchId(batchId)
                .withMessages(List.of(batchMessage))
                .build();

        assertThat(messageBatch.getBatchId()).isEqualTo(batchId);
        assertThat(messageBatch.getMessages()).hasSize(1)
                .allSatisfy(message -> {
                    assertThat(message.getPartyId()).isEqualTo(partyId);
                    assertThat(message.getMessageId()).isEqualTo(messageId);
                    assertThat(message.getEmailName()).isEqualTo("Sundsvalls kommun");
                    assertThat(message.getSmsName()).isEqualTo("Sundsvall");
                    assertThat(message.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
                    assertThat(message.getSubject()).isEqualTo("subject");
                    assertThat(message.getMessage()).isEqualTo("message");
                });

        MessageBatchDto batch = messageBatch.toBuilder()
                .withBatchId("1")
                .build();

        assertThat(batch.getBatchId()).isEqualTo("1");
    }
}
