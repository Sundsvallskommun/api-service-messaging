package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.SnailmailDto;

public class SnailmailSenderIntegrationMapperTests {

    private final SnailmailSenderIntegrationMapper mapper = new SnailmailSenderIntegrationMapper();

    @Test
    void test_toSendSnailmailRequest_whenDtoIsNull() {
        assertThat(mapper.toSendSnailmailRequest(null)).isNull();
    }

    @Test
    void test_toSendSnailmailRequest() {
        var dto = SnailmailDto.builder()
                .withAttachments(List.of(SnailmailDto.AttachmentDto.builder()
                        .withName("someName")
                        .withContentType("someContentType")
                        .withContent("someContent")
                        .build()))
                .build();

        var request = mapper.toSendSnailmailRequest(dto);
        assertThat(request.getAttachments()).hasSize(1);
    }
}
