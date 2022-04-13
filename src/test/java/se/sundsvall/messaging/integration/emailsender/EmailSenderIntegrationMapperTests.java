package se.sundsvall.messaging.integration.emailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.model.Sender;

class EmailSenderIntegrationMapperTests {

    private final EmailSenderIntegrationMapper mapper = new EmailSenderIntegrationMapper();

    @Test
    void test_toSendEmailRequest_whenDtoIsNull() {
        assertThat(mapper.toSendEmailRequest(null)).isNull();
    }

    @Test
    void test_toSendEmailRequest() {
        var dto = EmailDto.builder()
            .withSender(Sender.Email.builder()
                .withName("someName")
                .withAddress("someAddress")
                .withReplyTo("someReplyTo")
                .build())
            .withEmailAddress("someEmailAddress")
            .withSubject("someSubject")
            .withMessage("someMessage")
            .withHtmlMessage("someHtmlMessage")
            .withAttachments(List.of(EmailDto.AttachmentDto.builder()
                .withName("someName")
                .withContentType("someContentType")
                .withContent("someContent")
                .build()))
            .build();

        var request = mapper.toSendEmailRequest(dto);

        assertThat(request.getSender().getName()).isEqualTo("someName");
        assertThat(request.getSender().getAddress()).isEqualTo("someAddress");
        assertThat(request.getSender().getReplyTo()).isEqualTo("someReplyTo");
        assertThat(request.getEmailAddress()).isEqualTo("someEmailAddress");
        assertThat(request.getSubject()).isEqualTo("someSubject");
        assertThat(request.getMessage()).isEqualTo("someMessage");
        assertThat(request.getHtmlMessage()).isEqualTo("someHtmlMessage");
        assertThat(request.getAttachments()).hasSize(1);
    }
}
