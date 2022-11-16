package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.DigitalMailDto;

import generated.se.sundsvall.digitalmailsender.Attachment;
import generated.se.sundsvall.digitalmailsender.BodyInformation;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.SupportInfo;

@Component
class DigitalMailSenderIntegrationMapper {

    DigitalMailRequest toDigitalMailRequest(final DigitalMailDto dto) {
        if (dto == null) {
            return null;
        }

        var attachments = Optional.ofNullable(dto.getAttachments())
            .map(dtoAttachments -> dtoAttachments.stream()
                .map(attachment -> new Attachment()
                    .contentType(attachment.getContentType().getValue())
                    .body(attachment.getContent())
                    .filename(attachment.getFilename()))
                .toList())
            .orElse(null);

        return new DigitalMailRequest()
            .partyId(dto.getPartyId())
            .municipalityId(dto.getSender().getMunicipalityId())
            .headerSubject(dto.getSubject())
            .supportInfo(new SupportInfo()
                .supportText(dto.getSender().getSupportInfo().getText())
                .contactInformationEmail(dto.getSender().getSupportInfo().getEmailAddress())
                .contactInformationPhoneNumber(dto.getSender().getSupportInfo().getPhoneNumber())
                .contactInformationUrl(dto.getSender().getSupportInfo().getUrl()))
            .bodyInformation(new BodyInformation()
                .contentType(dto.getContentType().getValue())
                .body(dto.getBody()))
            .attachments(attachments);
    }
}
