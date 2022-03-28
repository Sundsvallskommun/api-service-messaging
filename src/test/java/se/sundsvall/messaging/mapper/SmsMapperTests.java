package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class SmsMapperTests {

    @Test
    void toEntity_givenSmsRequest_returnsNullWhenSmsRequestIsNull() {
        assertThat(SmsMapper.toEntity((IncomingSmsRequest) null)).isNull();
    }

    @Test
    void toEntity_givenSmsRequest_returnsSmsEntityWithSameValue() {
        var request = IncomingSmsRequest.builder()
            .withParty(Party.builder()
                .withPartyId("1")
                .build())
            .withSender("Test")
            .withMobileNumber("Test")
            .withMessage("Test")
            .build();

        var smsEntity = SmsMapper.toEntity(request);

        assertThat(smsEntity.getPartyId()).isEqualTo(request.getParty().getPartyId());
        assertThat(smsEntity.getSender()).isEqualTo(request.getSender());
        assertThat(smsEntity.getMobileNumber()).isEqualTo(request.getMobileNumber());
    }

    @Test
    void toDto_givenSmsEntity_returnsNullWhenSmsEntityIsNull() {
        assertThat(SmsMapper.toDto(null)).isNull();
    }

    @Test
    void toDto_givenSmsEntity_returnsSmsDtoWithSameValue() {
        var smsEntity = SmsEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withSender("Test")
            .withPartyId("1")
            .withExternalReferences(Map.of("key", "value"))
            .withMobileNumber("Test")
            .withStatus(MessageStatus.PENDING)
            .withMessage("Test")
            .build();

        var smsDto = SmsMapper.toDto(smsEntity);

        assertThat(smsDto.getBatchId()).isEqualTo(smsEntity.getBatchId());
        assertThat(smsDto.getMessageId()).isEqualTo(smsEntity.getMessageId());
        assertThat(smsDto.getSender()).isEqualTo(smsEntity.getSender());
        assertThat(smsDto.getParty().getPartyId()).isEqualTo(smsEntity.getPartyId());
        assertThat(smsDto.getParty().getExternalReferences()).hasSameSizeAs(smsEntity.getExternalReferences().entrySet());
        assertThat(smsDto.getMobileNumber()).isEqualTo(smsEntity.getMobileNumber());
        assertThat(smsDto.getStatus()).isEqualTo(smsEntity.getStatus());
        assertThat(smsDto.getMessage()).isEqualTo(smsEntity.getMessage());
    }
}
