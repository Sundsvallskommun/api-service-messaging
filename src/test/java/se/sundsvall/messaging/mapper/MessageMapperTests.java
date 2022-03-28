package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

class MessageMapperTests {
    
    private static final String BATCH_ID = UUID.randomUUID().toString();

    @Test
    void toMessageBatch_whenMessageEntityListIsNull_thenReturnNull() {
        assertThat(MessageMapper.toMessageBatch((List<MessageEntity>) null)).isNull();
    }

    @Test
    void toMessageBatch_whenBatchIdIsMissing_thenReturnMessageBatchWithEmptyList() {
        var entityMissingBatch = createMessageEntity().toBuilder()
            .withBatchId(null)
            .build();

        var messageBatchDto = MessageMapper.toMessageBatch(List.of(entityMissingBatch));

        assertThat(MessageMapper.toMessageBatch(List.of()).getMessages()).isEmpty();
        assertThat(messageBatchDto.getBatchId()).isNull();
    }

    @Test
    void toMessageBatch_givenListOfMessageEntity_thenReturnMessageBatchDto() {
        var entityList = List.of(createMessageEntity());

        var messageBatchDto = MessageMapper.toMessageBatch(entityList);

        assertThat(messageBatchDto.getBatchId()).isEqualTo(BATCH_ID);
    }

    @Test
    void toMessageBatch_givenNullMessageRequest_thenReturnNull() {
        assertThat(MessageMapper.toMessageBatch((MessageRequest) null)).isNull();
        assertThat(MessageMapper.toMessageBatch(MessageRequest.builder().build())).isNull();
    }

    @Test
    void toMessageBatch_givenMessageRequest_thenReturnMessageBatchDto() {
        var request = MessageRequest.builder()
            .withMessages(List.of(MessageRequest.Message.builder()
                .withSubject("subject")
                .withSenderEmail("sender")
                .withParty(Party.builder()
                    .withPartyId(UUID.randomUUID().toString())
                    .build())
                .withMessage("message")
                .withEmailName("email")
                .withSmsName("sms")
                .build()))
            .build();

        var messageBatchDto = MessageMapper.toMessageBatch(request);

        assertThat(messageBatchDto.getMessages()).hasSameSizeAs(request.getMessages());
        assertThat(messageBatchDto.getBatchId()).isNotNull();
        assertThat(messageBatchDto.getMessages()).allMatch(message -> message.getMessageId() != null);
    }

    @Test
    void toEntity_givenNullMessage_thenReturnNull() {
        assertThat(MessageMapper.toEntity( null, BATCH_ID)).isNull();
    }

    @Test
    void toEntity_givenMessageAndBatchId_returnMessageEntity() {
        var message = MessageBatchDto.Message.builder()
            .withSenderEmail("sender@hotmail.com")
            .withMessage("message")
            .withSubject("subject")
            .withParty(Party.builder()
                .withPartyId(UUID.randomUUID().toString())
                .build())
            .withMessageId(UUID.randomUUID().toString())
            .withSmsName("sms")
            .withEmailName("email")
            .build();

        var messageEntity = MessageMapper.toEntity(message, BATCH_ID);

        assertThat(messageEntity.getBatchId()).isEqualTo(BATCH_ID);
        assertThat(messageEntity.getMessage()).isEqualTo(message.getMessage());
        assertThat(messageEntity.getPartyId()).isEqualTo(message.getParty().getPartyId());
        assertThat(messageEntity.getEmailName()).isEqualTo(message.getEmailName());
        assertThat(messageEntity.getSubject()).isEqualTo(message.getSubject());
        assertThat(messageEntity.getSenderEmail()).isEqualTo(message.getSenderEmail());
        assertThat(messageEntity.getSmsName()).isEqualTo(message.getSmsName());
    }

    @Test
    void toDto_givenNullMessageEntity_thenReturnNull() {
        assertThat(MessageMapper.toDto(null)).isNull();
    }

    @Test
    void toDto_givenMessageEntity_returnMessageDto() {
        var messageEntity = createMessageEntity();

        var message = MessageMapper.toDto(messageEntity);

        assertThat(message.getMessage()).isEqualTo(messageEntity.getMessage());
        assertThat(message.getMessageId()).isEqualTo(messageEntity.getMessageId());
        assertThat(message.getParty().getPartyId()).isEqualTo(messageEntity.getPartyId());
        assertThat(message.getParty().getExternalReferences()).hasSize(1);
        assertThat(message.getEmailName()).isEqualTo(messageEntity.getEmailName());
        assertThat(message.getSmsName()).isEqualTo(messageEntity.getSmsName());
        assertThat(message.getSenderEmail()).isEqualTo(messageEntity.getSenderEmail());
        assertThat(message.getSubject()).isEqualTo(messageEntity.getSubject());
    }

    private MessageEntity createMessageEntity() {
        return MessageEntity.builder()
                .withMessageId(UUID.randomUUID().toString())
                .withEmailName("name")
                .withMessage("message")
                .withSenderEmail("sender@hotmail.com")
                .withMessageStatus(MessageStatus.SENT)
                .withBatchId(BATCH_ID)
                .withPartyId(UUID.randomUUID().toString())
                .withExternalReferences(Map.of("key", "value"))
                .withMessageType(MessageType.EMAIL)
                .withSmsName("sms")
                .build();
    }
}
