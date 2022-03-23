package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;
import se.sundsvall.messaging.model.dto.HistoryDto;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.model.entity.HistoryEntity;
import se.sundsvall.messaging.model.entity.SmsEntity;

class HistoryMapperTest {

    @Test
    void toHistory_givenNullEmailEntity_thenReturnsNull() {
        assertThat(HistoryMapper.toHistory((EmailEntity) null)).isNull();
    }

    @Test
    void toHistory_givenEmailEntity_thenHistoryWithStatus_SENT() {
        EmailEntity email = EmailEntity.builder()
                .withBatchId("batch")
                .withEmailAddress("test@hotmail.com")
                .withPartyId("party")
                .withSenderEmail("test@sender")
                .withMessage("message")
                .withMessageId("messageid")
                .withSenderName("sender")
                .withStatus(MessageStatus.PENDING)
                .withSubject("subject")
                .build();

        HistoryEntity history = HistoryMapper.toHistory(email);

        assertThat(history.getId()).isNotNull();
        assertThat(history.getBatchId()).isEqualTo(email.getBatchId());
        assertThat(history.getMessage()).isEqualTo(email.getMessage());
        assertThat(history.getMessageId()).isEqualTo(email.getMessageId());
        assertThat(history.getPartyId()).isEqualTo(email.getPartyId());
        assertThat(history.getSender()).isEqualTo(email.getSenderName());
        assertThat(history.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(history.getPartyContact()).isEqualTo(email.getEmailAddress());
        assertThat(history.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    void toHistory_givenNullSmsEntity_thenReturnsNull() {
        assertThat(HistoryMapper.toHistory((SmsEntity) null)).isNull();
    }

    @Test
    void toHistory_givenSmsEntity_thenHistoryWithStatus_SENT() {
        SmsEntity sms = SmsEntity.builder()
                .withStatus(MessageStatus.PENDING)
                .withMessage("message")
                .withMobileNumber("mobilenumber")
                .withPartyId("partyid")
                .withSender("sender")
                .withMessageId("messageid")
                .withBatchId("batch")
                .build();

        HistoryEntity history = HistoryMapper.toHistory(sms);

        assertThat(history.getId()).isNotNull();
        assertThat(history.getCreatedAt()).isNotNull();
        assertThat(history.getMessageId()).isEqualTo(sms.getMessageId());
        assertThat(history.getMessage()).isEqualTo(sms.getMessage());
        assertThat(history.getSender()).isEqualTo(sms.getSender());
        assertThat(history.getPartyId()).isEqualTo(sms.getPartyId());
        assertThat(history.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(history.getMessageType()).isEqualTo(MessageType.SMS);
        assertThat(history.getPartyContact()).isEqualTo(sms.getMobileNumber());
        assertThat(history.getBatchId()).isEqualTo(sms.getBatchId());
    }

    @Test
    void toHistory_givenNullUndeliverableMessageDto_thenReturnsNull() {
        assertThat(HistoryMapper.toHistory((UndeliverableMessageDto) null)).isNull();
    }

    @Test
    void toHistory_givenUndeliverableMessageDto_thenReturnHistoryEntity() {
        UndeliverableMessageDto undeliverable = UndeliverableMessageDto.builder()
                .withBatchId("batch")
                .withContent("content")
                .withMessageId("messageid")
                .withPartyContact("partycontact")
                .withSubject("subject")
                .withStatus(MessageStatus.FAILED)
                .withType(MessageType.UNDELIVERABLE)
                .withSenderName("sendername")
                .withSenderEmail("sender@test.com")
                .withPartyId("partyid")
                .build();

        HistoryEntity history = HistoryMapper.toHistory(undeliverable);

        assertThat(history.getId()).isNotNull();
        assertThat(history.getBatchId()).isEqualTo(undeliverable.getBatchId());
        assertThat(history.getMessageId()).isEqualTo(undeliverable.getMessageId());
        assertThat(history.getPartyId()).isEqualTo(undeliverable.getPartyId());
        assertThat(history.getPartyContact()).isEqualTo(undeliverable.getPartyContact());
        assertThat(history.getSender()).isEqualTo(undeliverable.getSenderName());
        assertThat(history.getMessage()).isEqualTo(undeliverable.getContent());
        assertThat(history.getStatus()).isEqualTo(undeliverable.getStatus());
        assertThat(history.getMessageType()).isEqualTo(undeliverable.getType());
        assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    void toHistoryDto_givenNullHistoryEntity_thenReturnNull() {
         assertThat(HistoryMapper.toHistoryDto((HistoryEntity) null)).isNull();
    }

    @Test
    void toHistoryDto_givenHistoryEntity_thenReturnHistoryDtoWithSameValues() {
        LocalDateTime createdAt = LocalDateTime.now();

        HistoryEntity entity = HistoryEntity.builder()
                .withId(UUID.randomUUID().toString())
                .withBatchId("batch")
                .withMessage("message")
                .withPartyContact("partycontact")
                .withMessageId("messageid")
                .withMessageType(MessageType.SMS)
                .withPartyId("partyid")
                .withSender("sender")
                .withStatus(MessageStatus.PENDING)
                .withCreatedAt(createdAt)
                .build();

        HistoryDto dto = HistoryMapper.toHistoryDto(entity);

        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getBatchId()).isEqualTo(entity.getBatchId());
        assertThat(dto.getSender()).isEqualTo(entity.getSender());
        assertThat(dto.getPartyId()).isEqualTo(entity.getPartyId());
        assertThat(dto.getPartyContact()).isEqualTo(entity.getPartyContact());
        assertThat(dto.getMessage()).isEqualTo(entity.getMessage());
        assertThat(dto.getMessageId()).isEqualTo(entity.getMessageId());
        assertThat(dto.getMessageType()).isEqualTo(entity.getMessageType());
        assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
    }
}
