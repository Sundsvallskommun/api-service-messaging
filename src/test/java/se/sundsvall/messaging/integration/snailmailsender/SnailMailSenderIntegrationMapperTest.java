package se.sundsvall.messaging.integration.snailmailsender;

import generated.se.sundsvall.snailmail.Attachment;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class SnailMailSenderIntegrationMapperTest {

    private final SnailMailSenderIntegrationMapper mapper = new SnailMailSenderIntegrationMapper();

    @Test
    void test_toSendSnailmailRequest_whenRequestIsNull() {
        assertThat(mapper.toSendSnailmailRequest(null)).isNull();
    }

    @Test
    void test_toSendSnailmailRequest() {
        var dto = SnailMailDto.builder()
            .withAttachments(List.of(SnailMailDto.Attachment.builder()
                .withName("someName")
                .withContentType(Attachment.ContentTypeEnum.APPLICATION_PDF.getValue())
                .withContent("someContent")
                .build()))
            .build();

        var mappedRequest = mapper.toSendSnailmailRequest(dto);

        assertThat(mappedRequest.getAttachments()).hasSize(1);
    }
}
