package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.Sender;

class DigitalMailSenderIntegrationMapperTests {

    private final DigitalMailSenderIntegrationMapper mapper = new DigitalMailSenderIntegrationMapper();

    @Test
    void test_toDigitalMailRequest_whenDtoIsNull() {
        assertThat(mapper.toDigitalMailRequest(null)).isNull();
    }

    @Test
    void test_toDigitalMailRequest() {
        var dto = DigitalMailDto.builder()
            .withSender(Sender.DigitalMail.builder()
                .withMunicipalityId("someMunicipalityId")
                .withSupportInfo(Sender.DigitalMail.SupportInfo.builder()
                    .withEmailAddress("someEmailAddress")
                    .withPhoneNumber("somePhoneNumber")
                    .withText("someText")
                    .withUrl("someUrl")
                    .build())
                .build())
            .withPartyId("somePartyId")
            .withSubject("someSubject")
            .withContentType(ContentType.TEXT_PLAIN)
            .withBody("someBody")
            .withAttachments(List.of(DigitalMailDto.AttachmentDto.builder()
                .withFilename("someFilename")
                .withContentType(ContentType.APPLICATION_PDF)
                .withContent("someContent")
                .build()))
            .build();

        var request = mapper.toDigitalMailRequest(dto);

        assertThat(request.getHeaderSubject()).isEqualTo("someSubject");
        assertThat(request.getMunicipalityId()).isEqualTo("someMunicipalityId");
        assertThat(request.getBodyInformation()).satisfies(bodyInformation -> {
            assertThat(bodyInformation.getContentType()).isEqualTo(ContentType.TEXT_PLAIN.getValue());
            assertThat(bodyInformation.getBody()).isEqualTo(dto.getBody());
        });
        assertThat(request.getAttachments()).hasSameSizeAs(dto.getAttachments());
        assertThat(request.getAttachments().get(0)).satisfies(attachment -> {
            assertThat(attachment.getFilename()).isEqualTo("someFilename");
            assertThat(attachment.getContentType()).isEqualTo(ContentType.APPLICATION_PDF.getValue());
            assertThat(attachment.getBody()).isEqualTo("someContent");
        });
    }
}
