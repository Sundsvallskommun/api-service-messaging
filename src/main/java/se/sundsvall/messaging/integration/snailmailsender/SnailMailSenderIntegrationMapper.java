package se.sundsvall.messaging.integration.snailmailsender;


import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.EnvelopeType;

import generated.se.sundsvall.snailmail.Attachment;
import generated.se.sundsvall.snailmail.SendSnailMailRequest;

@Component
class SnailMailSenderIntegrationMapper {

    SendSnailMailRequest toSendSnailmailRequest(final SnailMailDto dto) {
        if (dto == null) {
            return null;
        }

        return new SendSnailMailRequest()
            .department(dto.department())
            .deviation(dto.deviation())
            .attachments(ofNullable(dto.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> new Attachment()
                        .name(attachment.name())
                        .contentType(attachment.contentType())
                        .content(attachment.content())
                        .envelopeType(toEnvelopeTypeEnum(attachment.envelopeType())))
                    .toList()
                )
                .orElse(null));
    }

    private Attachment.EnvelopeTypeEnum toEnvelopeTypeEnum(EnvelopeType envelopeType) {
        if(envelopeType != null) {
            return Attachment.EnvelopeTypeEnum.fromValue(envelopeType.name());
        }
        return null;
    }
}
