package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.model.dto.SmsDto;
import se.sundsvall.messaging.model.entity.SmsEntity;

import generated.se.sundsvall.smssender.SmsRequest;

class SmsMapperTest {

    @Test
    void toRequest_givenSmsEntity_returnsNullWhenSmsRequestIsNull() {
        assertThat(SmsMapper.toRequest((SmsEntity) null)).isNull();
    }

    @Test
    void toRequest_givenSmsEntity_returnsSmsEntityWithSameValue() {
        SmsEntity entity = SmsEntity.builder()
                .withMessage("test")
                .withMobileNumber("070123456")
                .withSender("test sender")
                .withPartyId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withBatchId(UUID.randomUUID().toString())
                .withStatus(MessageStatus.PENDING)
                .withCreatedAt(LocalDateTime.now())
                .build();

        SmsRequest request = SmsMapper.toRequest(entity);

        assertThat(request.getMessage()).isEqualTo(entity.getMessage());
        assertThat(request.getSender()).isEqualTo(entity.getSender());
        assertThat(request.getMobileNumber()).isEqualTo(entity.getMobileNumber());
    }

    @Test
    void toEntity_givenSmsRequest_returnsNullWhenSmsRequestIsNull() {
        assertThat(SmsMapper.toEntity((IncomingSmsRequest) null)).isNull();
    }

    @Test
    void toEntity_givenSmsRequest_returnsSmsEntityWithSameValue() {
        IncomingSmsRequest request = IncomingSmsRequest.builder()
                .withPartyId("1")
                .withSender("Test")
                .withMobileNumber("Test")
                .withMessage("Test")
                .build();

        SmsEntity entity = SmsMapper.toEntity(request);

        assertThat(request.getPartyId()).isEqualTo(entity.getPartyId());
        assertThat(request.getSender()).isEqualTo(entity.getSender());
        assertThat(request.getMobileNumber()).isEqualTo(entity.getMobileNumber());
        assertThat(request.getMessage()).isEqualTo(entity.getMessage());

    }

    @Test
    void toEntity_givenSmsDto_returnsNullWhenSmsDtoIsNull() {
        assertThat(SmsMapper.toEntity((SmsDto) null)).isNull();
    }

    @Test
    void toEntity_givenSmsDto_returnsSmsEntityWithSameValue() {
        SmsDto dto = SmsDto.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withSender("Test")
                .withPartyId("1")
                .withMobileNumber("Test")
                .withMessage("Test")
                .withStatus(MessageStatus.PENDING)
                .build();

        SmsEntity entity = SmsMapper.toEntity(dto);

        assertThat(dto.getBatchId()).isEqualTo(entity.getBatchId());
        assertThat(dto.getMessageId()).isEqualTo(entity.getMessageId());
        assertThat(dto.getSender()).isEqualTo(entity.getSender());
        assertThat(dto.getPartyId()).isEqualTo(entity.getPartyId());
        assertThat(dto.getMobileNumber()).isEqualTo(entity.getMobileNumber());
        assertThat(dto.getMessage()).isEqualTo(entity.getMessage());
        assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
    }


    @Test
    void toDto_givenSmsEntity_returnsNullWhenSmsEntityIsNull() {
        assertThat(SmsMapper.toDto((SmsEntity) null)).isNull();
    }

    @Test
    void toDto_givenSmsEntity_returnsSmsDtoWithSameValue() {

        SmsEntity entity = SmsEntity.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withSender("Test")
                .withPartyId("1")
                .withMobileNumber("Test")
                .withStatus(MessageStatus.PENDING)
                .withMessage("Test")
                .build();

        SmsDto dto = SmsMapper.toDto(entity);

        assertThat(entity.getBatchId()).isEqualTo(dto.getBatchId());
        assertThat(entity.getMessageId()).isEqualTo(dto.getMessageId());
        assertThat(entity.getSender()).isEqualTo(dto.getSender());
        assertThat(entity.getPartyId()).isEqualTo(dto.getPartyId());
        assertThat(entity.getMobileNumber()).isEqualTo(dto.getMobileNumber());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getMessage()).isEqualTo(dto.getMessage());
    }
}
