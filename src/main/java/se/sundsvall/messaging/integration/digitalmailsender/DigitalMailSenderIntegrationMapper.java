package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import se.sundsvall.messaging.dto.DigitalMailDto;

import generated.se.sundsvall.digitalmailsender.Attachment;
import generated.se.sundsvall.digitalmailsender.BodyInformation;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.SupportInfo;

@Component
class DigitalMailSenderIntegrationMapper {

    private final DigitalMailSenderIntegrationProperties properties;

    DigitalMailSenderIntegrationMapper(final DigitalMailSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    DigitalMailRequest toDigitalMailRequest(final DigitalMailDto dto) {
        if (dto == null) {
            return null;
        }

        List<Attachment> attachments = null;
        if (!CollectionUtils.isEmpty(dto.getAttachments())) {
            attachments = Optional.ofNullable(dto.getAttachments()).stream()
                    .flatMap(Collection::stream)
                    .map(attachment -> new Attachment()
                            .contentType(attachment.getContentType().getValue())
                            .body(attachment.getContent())
                            .filename(attachment.getFilename()))
                    .toList();
        }

        return new DigitalMailRequest()
                .partyId(dto.getPartyId())
                .municipalityId(properties.getDefaults().getMunicipalityId())
                .headerSubject(Optional.ofNullable(dto.getSubject()).orElse(properties.getDefaults().getSubject()))
                .supportInfo(new SupportInfo()
                        .supportText(properties.getDefaults().getSupportInfo().getText())
                        .contactInformationEmail(properties.getDefaults().getSupportInfo().getEmailAddress())
                        .contactInformationPhoneNumber(properties.getDefaults().getSupportInfo().getPhoneNumber())
                        .contactInformationUrl(properties.getDefaults().getSupportInfo().getUrl()))
                .bodyInformation(new BodyInformation()
                        .contentType(dto.getContentType().getValue())
                        .body(dto.getBody()))
                .attachments(attachments);
    }
}
