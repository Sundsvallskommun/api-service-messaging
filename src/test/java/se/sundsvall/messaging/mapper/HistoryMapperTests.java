package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.HistoryDto;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

class HistoryMapperTests {

    @Test
    void toHistory_givenNullEmailEntity_thenReturnsNull() {
        assertThat(HistoryMapper.toHistory((EmailEntity) null)).isNull();
    }

    @Test
    void toHistory_givenEmailEntity_thenHistoryWithStatus_SENT() {
        var emailEntity = EmailEntity.builder()
            .withBatchId("batch")
            .withEmailAddress("test@hotmail.com")
            .withPartyId("party")
            .withExternalReferences(Map.of("key", "value"))
            .withSenderEmail("test@sender")
            .withMessage("message")
            .withMessageId("messageid")
            .withSenderName("sender")
            .withStatus(MessageStatus.PENDING)
            .withSubject("subject")
            .build();

        var historyEntity = HistoryMapper.toHistory(emailEntity);

        assertThat(historyEntity.getBatchId()).isEqualTo(emailEntity.getBatchId());
        assertThat(historyEntity.getMessage()).isEqualTo(emailEntity.getMessage());
        assertThat(historyEntity.getMessageId()).isEqualTo(emailEntity.getMessageId());
        assertThat(historyEntity.getPartyId()).isEqualTo(emailEntity.getPartyId());
        assertThat(historyEntity.getExternalReferences()).hasSize(1);
        assertThat(historyEntity.getSender()).isEqualTo(emailEntity.getSenderName());
        assertThat(historyEntity.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(historyEntity.getPartyContact()).isEqualTo(emailEntity.getEmailAddress());
        assertThat(historyEntity.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(historyEntity.getCreatedAt()).isNotNull();
    }

    @Test
    void toHistory_givenNullSmsEntity_thenReturnsNull() {
        assertThat(HistoryMapper.toHistory((SmsEntity) null)).isNull();
    }

    @Test
    void toHistory_givenSmsEntity_thenHistoryWithStatus_SENT() {
        var smsEntity = SmsEntity.builder()
            .withStatus(MessageStatus.PENDING)
            .withMessage("message")
            .withMobileNumber("mobilenumber")
            .withPartyId("partyid")
            .withExternalReferences(Map.of("key", "value"))
            .withSender("sender")
            .withMessageId("messageid")
            .withBatchId("batch")
            .build();

        var historyEntity = HistoryMapper.toHistory(smsEntity);

        assertThat(historyEntity.getCreatedAt()).isNotNull();
        assertThat(historyEntity.getMessageId()).isEqualTo(smsEntity.getMessageId());
        assertThat(historyEntity.getMessage()).isEqualTo(smsEntity.getMessage());
        assertThat(historyEntity.getSender()).isEqualTo(smsEntity.getSender());
        assertThat(historyEntity.getPartyId()).isEqualTo(smsEntity.getPartyId());
        assertThat(historyEntity.getExternalReferences()).hasSize(1);
        assertThat(historyEntity.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(historyEntity.getMessageType()).isEqualTo(MessageType.SMS);
        assertThat(historyEntity.getPartyContact()).isEqualTo(smsEntity.getMobileNumber());
        assertThat(historyEntity.getBatchId()).isEqualTo(smsEntity.getBatchId());
    }

    @Test
    void toHistory_givenNullUndeliverableMessageDto_thenReturnsNull() {
        assertThat(HistoryMapper.toHistory((UndeliverableMessageDto) null)).isNull();
    }

    @Test
    void toHistory_givenUndeliverableMessageDto_thenReturnHistoryEntity() {
        var undeliverableMessageDto = UndeliverableMessageDto.builder()
            .withBatchId("batch")
            .withContent("content")
            .withMessageId("messageid")
            .withPartyContact("partycontact")
            .withSubject("subject")
            .withStatus(MessageStatus.FAILED)
            .withType(MessageType.UNDELIVERABLE)
            .withSenderName("sendername")
            .withSenderEmail("sender@test.com")
            .withParty(Party.builder()
                .withPartyId("partyId")
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()))
                .build())
            .build();

        var historyEntity = HistoryMapper.toHistory(undeliverableMessageDto);

        assertThat(historyEntity.getBatchId()).isEqualTo(undeliverableMessageDto.getBatchId());
        assertThat(historyEntity.getMessageId()).isEqualTo(undeliverableMessageDto.getMessageId());
        assertThat(historyEntity.getPartyId()).isEqualTo(undeliverableMessageDto.getParty().getPartyId());
        assertThat(historyEntity.getExternalReferences()).hasSize(1);
        assertThat(historyEntity.getPartyContact()).isEqualTo(undeliverableMessageDto.getPartyContact());
        assertThat(historyEntity.getSender()).isEqualTo(undeliverableMessageDto.getSenderName());
        assertThat(historyEntity.getMessage()).isEqualTo(undeliverableMessageDto.getContent());
        assertThat(historyEntity.getStatus()).isEqualTo(undeliverableMessageDto.getStatus());
        assertThat(historyEntity.getMessageType()).isEqualTo(undeliverableMessageDto.getType());
        assertThat(historyEntity.getCreatedAt()).isNotNull();
    }

    @Test
    void toHistoryDto_givenNullHistoryEntity_thenReturnNull() {
         assertThat(HistoryMapper.toHistoryDto((HistoryEntity) null)).isNull();
    }

    @Test
    void toHistoryDto_givenHistoryEntity_thenReturnHistoryDtoWithSameValues() {
        var now = LocalDateTime.now();

        var historyEntity = HistoryEntity.builder()
            .withBatchId("batch")
            .withMessage("message")
            .withPartyContact("partycontact")
            .withMessageId("messageid")
            .withMessageType(MessageType.SMS)
            .withPartyId("partyid")
            .withExternalReferences(Map.of("key", "value"))
            .withSender("sender")
            .withStatus(MessageStatus.PENDING)
            .withCreatedAt(now)
            .build();

        HistoryDto dto = HistoryMapper.toHistoryDto(historyEntity);

        assertThat(dto.getBatchId()).isEqualTo(historyEntity.getBatchId());
        assertThat(dto.getSender()).isEqualTo(historyEntity.getSender());
        assertThat(dto.getParty().getPartyId()).isEqualTo(historyEntity.getPartyId());
        assertThat(dto.getParty().getExternalReferences()).hasSize(1);
        assertThat(dto.getPartyContact()).isEqualTo(historyEntity.getPartyContact());
        assertThat(dto.getMessage()).isEqualTo(historyEntity.getMessage());
        assertThat(dto.getMessageId()).isEqualTo(historyEntity.getMessageId());
        assertThat(dto.getMessageType()).isEqualTo(historyEntity.getMessageType());
        assertThat(dto.getStatus()).isEqualTo(historyEntity.getStatus());
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }
}
