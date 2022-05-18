package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.model.ContentType;

import generated.se.sundsvall.digitalmailsender.Attachment;
import generated.se.sundsvall.digitalmailsender.BodyInformation;

@ExtendWith(MockitoExtension.class)
class DigitalMailSenderIntegrationMapperTests {

    @Mock
    private DigitalMailSenderIntegrationProperties mockProperties;
    @Mock
    private DigitalMailSenderIntegrationProperties.Defaults mockDefaults;
    @Mock
    private DigitalMailSenderIntegrationProperties.Defaults.SupportInfo mockSupportInfo;

    private DigitalMailSenderIntegrationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DigitalMailSenderIntegrationMapper(mockProperties);
    }

    @Test
    void test_toDigitalMailRequest_whenDtoIsNull() {
        assertThat(mapper.toDigitalMailRequest(null)).isNull();
    }

    @Test
    void test_toDigitalMailRequest() {
        when(mockProperties.getDefaults()).thenReturn(mockDefaults);
        when(mockDefaults.getMunicipalityId()).thenReturn("someMunicipalityId");
        when(mockDefaults.getSubject()).thenReturn("someDefaultSubject");
        when(mockDefaults.getSupportInfo()).thenReturn(mockSupportInfo);
        when(mockSupportInfo.getText()).thenReturn("someText");
        when(mockSupportInfo.getEmailAddress()).thenReturn("someEmailAddress");
        when(mockSupportInfo.getUrl()).thenReturn("someUrl");
        when(mockSupportInfo.getPhoneNumber()).thenReturn("somePhoneNumber");

        var dto = DigitalMailDto.builder()
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
            assertThat(bodyInformation.getContentType()).isEqualTo(BodyInformation.ContentTypeEnum.PLAIN);
            assertThat(bodyInformation.getBody()).isEqualTo(dto.getBody());
        });
        assertThat(request.getAttachments()).hasSameSizeAs(dto.getAttachments());
        assertThat(request.getAttachments().get(0)).satisfies(attachment -> {
            assertThat(attachment.getFilename()).isEqualTo("someFilename");
            assertThat(attachment.getContentType()).isEqualTo(Attachment.ContentTypeEnum.APPLICATION_PDF);
            assertThat(attachment.getBody()).isEqualTo("someContent");
        });
    }

    @Test
    void test_toDigitalMailRequestWhenSubjectIsMissing() {
        when(mockProperties.getDefaults()).thenReturn(mockDefaults);
        when(mockDefaults.getMunicipalityId()).thenReturn("someMunicipalityId");
        when(mockDefaults.getSubject()).thenReturn("someDefaultSubject");
        when(mockDefaults.getSupportInfo()).thenReturn(mockSupportInfo);
        when(mockSupportInfo.getText()).thenReturn("someText");
        when(mockSupportInfo.getEmailAddress()).thenReturn("someEmailAddress");
        when(mockSupportInfo.getUrl()).thenReturn("someUrl");
        when(mockSupportInfo.getPhoneNumber()).thenReturn("somePhoneNumber");

        var dto = DigitalMailDto.builder()
            .withPartyId("somePartyId")
            .withContentType(ContentType.TEXT_PLAIN)
            .withBody("someBody")
            .withAttachments(List.of(DigitalMailDto.AttachmentDto.builder()
                .withFilename("someFilename")
                .withContentType(ContentType.APPLICATION_PDF)
                .withContent("someContent")
                .build()))
            .build();

        var request = mapper.toDigitalMailRequest(dto);

        assertThat(request.getHeaderSubject()).isEqualTo("someDefaultSubject");
        assertThat(request.getMunicipalityId()).isEqualTo("someMunicipalityId");
        assertThat(request.getBodyInformation()).satisfies(bodyInformation -> {
            assertThat(bodyInformation.getContentType()).isEqualTo(BodyInformation.ContentTypeEnum.PLAIN);
            assertThat(bodyInformation.getBody()).isEqualTo(dto.getBody());
        });
        assertThat(request.getAttachments()).hasSameSizeAs(dto.getAttachments());
        assertThat(request.getAttachments().get(0)).satisfies(attachment -> {
            assertThat(attachment.getFilename()).isEqualTo("someFilename");
            assertThat(attachment.getContentType()).isEqualTo(Attachment.ContentTypeEnum.APPLICATION_PDF);
            assertThat(attachment.getBody()).isEqualTo("someContent");
        });
    }
}
