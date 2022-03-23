package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;
import se.sundsvall.messaging.api.request.MessageRequest;
import se.sundsvall.messaging.model.dto.MessageBatchDto;
import se.sundsvall.messaging.model.entity.MessageEntity;

class MessageMapperTest {
    
    private static final String BATCH_ID = UUID.randomUUID().toString();

    @Test
    void toMessageBatch_whenMessageEntityListIsNull_thenReturnNull() {
        assertThat(MessageMapper.toMessageBatch((List<MessageEntity>) null)).isNull();
    }

    @Test
    void toMessageBatch_whenBatchIdIsMissing_thenReturnMessageBatchWithEmptyList(){
        List<MessageEntity> emptyList = List.of();
        MessageEntity entityMissingBatch = createMessageEntity().toBuilder()
                .withBatchId(null)
                .build();

        MessageBatchDto batchMissingId = MessageMapper.toMessageBatch(List.of(entityMissingBatch));


        assertThat(MessageMapper.toMessageBatch(emptyList).getMessages()).isEmpty();
        assertThat(batchMissingId.getBatchId()).isNull();
    }

    @Test
    void toMessageBatch_givenListOfMessageEntity_thenReturnMessageBatchDto() {
        List<MessageEntity> entityList = List.of(createMessageEntity());

        MessageBatchDto batchDto = MessageMapper.toMessageBatch(entityList);

        assertThat(batchDto.getBatchId()).isEqualTo(BATCH_ID);
    }

    @Test
    void toMessageBatch_givenNullMessageRequest_thenReturnNull() {
        assertThat(MessageMapper.toMessageBatch((MessageRequest) null)).isNull();
        assertThat(MessageMapper.toMessageBatch(MessageRequest.builder().build())).isNull();
    }

    @Test
    void toMessageBatch_givenMessageRequest_thenReturnMessageBatchDto() {
        MessageRequest request = MessageRequest.builder()
                .withMessages(List.of(MessageRequest.Message.builder()
                                .withSubject("subject")
                                .withSenderEmail("sender")
                                .withPartyId(UUID.randomUUID().toString())
                                .withMessage("message")
                                .withEmailName("email")
                                .withSmsName("sms")
                        .build()))
                .build();
        MessageBatchDto dto = MessageMapper.toMessageBatch(request);
        assertThat(dto.getMessages()).hasSameSizeAs(request.getMessages());
        assertThat(dto.getBatchId()).isNotNull();
        assertThat(dto.getMessages()).allMatch(message -> message.getMessageId() != null);
    }

    @Test
    void toEntity_givenNullMessage_thenReturnNull() {
        assertThat(MessageMapper.toEntity( null, BATCH_ID)).isNull();
    }

    @Test
    void toEntity_givenMessageAndBatchId_returnMessageEntity() {
        MessageBatchDto.Message message = MessageBatchDto.Message.builder()
                .withSenderEmail("sender@hotmail.com")
                .withMessage("message")
                .withSubject("subject")
                .withPartyId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withSmsName("sms")
                .withEmailName("email")
                .build();

        MessageEntity entity = MessageMapper.toEntity(message, BATCH_ID);

        assertThat(entity.getBatchId()).isEqualTo(BATCH_ID);
        assertThat(entity.getMessage()).isEqualTo(message.getMessage());
        assertThat(entity.getPartyId()).isEqualTo(message.getPartyId());
        assertThat(entity.getEmailName()).isEqualTo(message.getEmailName());
        assertThat(entity.getSubject()).isEqualTo(message.getSubject());
        assertThat(entity.getSenderEmail()).isEqualTo(message.getSenderEmail());
        assertThat(entity.getSmsName()).isEqualTo(message.getSmsName());
    }

    @Test
    void toDto_givenNullMessageEntity_thenReturnNull() {
        assertThat(MessageMapper.toDto((MessageEntity) null)).isNull();
    }

    @Test
    void toDto_givenMessageEntity_returnMessageDto() {
        MessageEntity entity = createMessageEntity();

        MessageBatchDto.Message message = MessageMapper.toDto(entity);

        assertThat(message.getMessage()).isEqualTo(entity.getMessage());
        assertThat(message.getMessageId()).isEqualTo(entity.getMessageId());
        assertThat(message.getPartyId()).isEqualTo(entity.getPartyId());
        assertThat(message.getEmailName()).isEqualTo(entity.getEmailName());
        assertThat(message.getSmsName()).isEqualTo(entity.getSmsName());
        assertThat(message.getSenderEmail()).isEqualTo(entity.getSenderEmail());
        assertThat(message.getSubject()).isEqualTo(entity.getSubject());
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
                .withMessageType(MessageType.EMAIL)
                .withSmsName("sms")
                .build();
    }
}
