package se.sundsvall.messaging.mapper;

import java.util.UUID;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.model.dto.SmsDto;
import se.sundsvall.messaging.model.entity.SmsEntity;

import generated.se.sundsvall.smssender.SmsRequest;

public final class SmsMapper {

    private SmsMapper() {
    }

    public static SmsRequest toRequest(SmsEntity sms) {
        if (sms == null) {
            return null;
        }

        return new SmsRequest()
                .message(sms.getMessage())
                .mobileNumber(sms.getMobileNumber())
                .sender(sms.getSender());
    }

    public static SmsEntity toEntity(IncomingSmsRequest request) {
        if (request == null) {
            return null;
        }

        return SmsEntity.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withSender(request.getSender())
                .withPartyId(request.getPartyId())
                .withMobileNumber(request.getMobileNumber())
                .withMessage(request.getMessage())
                .withStatus(MessageStatus.PENDING)
                .build();
    }

    public static SmsEntity toEntity(SmsDto dto) {
        if (dto == null) {
            return null;
        }

        return SmsEntity.builder()
                .withBatchId(dto.getBatchId())
                .withMessageId(dto.getMessageId())
                .withSender(dto.getSender())
                .withPartyId(dto.getPartyId())
                .withMobileNumber(dto.getMobileNumber())
                .withMessage(dto.getMessage())
                .withStatus(dto.getStatus())
                .build();
    }

    public static SmsDto toDto(SmsEntity entity) {
        if (entity == null) {
            return null;
        }

        return SmsDto.builder()
                .withBatchId(entity.getBatchId())
                .withMessageId(entity.getMessageId())
                .withSender(entity.getSender())
                .withPartyId(entity.getPartyId())
                .withMobileNumber(entity.getMobileNumber())
                .withMessage(entity.getMessage())
                .withStatus(entity.getStatus())
                .build();
    }
}
