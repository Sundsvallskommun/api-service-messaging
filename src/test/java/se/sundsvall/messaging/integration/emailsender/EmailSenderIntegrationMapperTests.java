package se.sundsvall.messaging.integration.emailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationMapperTests {

    private final EmailSenderIntegrationMapper mapper = new EmailSenderIntegrationMapper();

    @Test
    void test_toSendEmailRequest_whenRequestIsNull() {
        assertThat(mapper.toSendEmailRequest(null)).isNull();
    }

    @Test
    void test_toSendEmailRequest() {
        var dto = EmailDto.builder()
            .withSender(EmailDto.Sender.builder()
                .withName("someName")
                .withAddress("someAddress")
                .withReplyTo("someReplyTo")
                .build())
            .withEmailAddress("someEmailAddress")
            .withSubject("someSubject")
            .withMessage("someMessage")
            .withHtmlMessage("someHtmlMessage")
            .withAttachments(List.of(EmailDto.Attachment.builder()
                .withName("someName")
                .withContentType("someContentType")
                .withContent("someContent")
                .build()))
            .build();

        var mappedRequest = mapper.toSendEmailRequest(dto);

        assertThat(mappedRequest.getSender().getName()).isEqualTo("someName");
        assertThat(mappedRequest.getSender().getAddress()).isEqualTo("someAddress");
        assertThat(mappedRequest.getSender().getReplyTo()).isEqualTo("someReplyTo");
        assertThat(mappedRequest.getEmailAddress()).isEqualTo("someEmailAddress");
        assertThat(mappedRequest.getSubject()).isEqualTo("someSubject");
        assertThat(mappedRequest.getMessage()).isEqualTo("someMessage");
        assertThat(mappedRequest.getHtmlMessage()).isEqualTo("someHtmlMessage");
        assertThat(mappedRequest.getAttachments()).hasSize(1);
    }
}
