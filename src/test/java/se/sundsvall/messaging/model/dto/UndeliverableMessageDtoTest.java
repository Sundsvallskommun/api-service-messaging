package se.sundsvall.messaging.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

class UndeliverableMessageDtoTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();

        UndeliverableMessageDto message = UndeliverableMessageDto.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withSenderEmail("noreply@sundsvall.se")
                .withSenderName("Sundsvalls kommun")
                .withPartyContact("john.doe@example.com")
                .withSubject("subject")
                .withContent("message")
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.SENT)
                .build();

        assertThat(message.getBatchId()).isEqualTo(batchId);
        assertThat(message.getMessageId()).isEqualTo(messageId);
        assertThat(message.getPartyId()).isEqualTo(partyId);
        assertThat(message.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(message.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(message.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(message.getSubject()).isEqualTo("subject");
        assertThat(message.getContent()).isEqualTo("message");
        assertThat(message.getType()).isEqualTo(MessageType.EMAIL);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.SENT);

        UndeliverableMessageDto undeliverableMessage = message.toBuilder()
                .withBatchId("1")
                .build();

        assertThat(undeliverableMessage.getBatchId()).isEqualTo("1");
    }

}
