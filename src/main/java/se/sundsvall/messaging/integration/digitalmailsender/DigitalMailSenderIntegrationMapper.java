package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.DigitalMailDto;

import generated.se.sundsvall.digitalmailsender.Attachment;
import generated.se.sundsvall.digitalmailsender.BodyInformation;
import generated.se.sundsvall.digitalmailsender.SecureDigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.SupportInfo;

@Component
class DigitalMailSenderIntegrationMapper {

    private final DigitalMailSenderIntegrationProperties properties;

    DigitalMailSenderIntegrationMapper(final DigitalMailSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    SecureDigitalMailRequest toDigitalMailRequest(final DigitalMailDto dto) {
        if (dto == null) {
            return null;
        }

        return new SecureDigitalMailRequest()
            .municipalityId(properties.getDefaults().getMunicipalityId())
            .headerSubject(Optional.ofNullable(dto.getSubject()).orElse(properties.getDefaults().getSubject()))
            .supportInfo(new SupportInfo()
                .supportText(properties.getDefaults().getSupportInfo().getText())
                .contactInformationEmail(properties.getDefaults().getSupportInfo().getEmailAddress())
                .contactInformationPhoneNumber(properties.getDefaults().getSupportInfo().getPhoneNumber())
                .contactInformationUrl(properties.getDefaults().getSupportInfo().getUrl()))
            .bodyInformation(new BodyInformation()
                .contentType(BodyInformation.ContentTypeEnum.fromValue(dto.getContentType().getValue()))
                .body(dto.getBody()))
            .attachments(Optional.ofNullable(dto.getAttachments()).orElse(List.of()).stream()
                .map(attachment -> new Attachment()
                    .contentType(Attachment.ContentTypeEnum.fromValue(attachment.getContentType().getValue()))
                    .body(attachment.getContent())
                    .filename(attachment.getFilename()))
                .toList());
    }
}
