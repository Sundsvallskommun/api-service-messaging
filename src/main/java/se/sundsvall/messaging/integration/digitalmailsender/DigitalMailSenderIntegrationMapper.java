package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Optional;

import org.springframework.stereotype.Component;

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

        return new DigitalMailRequest()
            .partyId(dto.partyId())
            .municipalityId(dto.sender().municipalityId())
            .headerSubject(dto.subject())
            .supportInfo(new SupportInfo()
                .supportText(dto.sender().supportInfo().text())
                .contactInformationEmail(dto.sender().supportInfo().emailAddress())
                .contactInformationPhoneNumber(dto.sender().supportInfo().phoneNumber())
                .contactInformationUrl(dto.sender().supportInfo().url()))
            .bodyInformation(new BodyInformation()
                .contentType(dto.contentType().getValue())
                .body(dto.body()))
            .attachments(Optional.ofNullable(dto.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> new Attachment()
                        .contentType(attachment.contentType().getValue())
                        .body(attachment.content())
                        .filename(attachment.filename()))
                    .toList())
                .orElse(null));
    }
}
