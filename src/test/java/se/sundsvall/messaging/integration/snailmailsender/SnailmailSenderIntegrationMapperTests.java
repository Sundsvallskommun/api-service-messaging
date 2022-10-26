package se.sundsvall.messaging.integration.snailmailsender;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.dto.SnailmailDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SnailmailSenderIntegrationMapperTests {

    private final SnailmailSenderIntegrationMapper mapper = new SnailmailSenderIntegrationMapper();

    @Test
    void test_toSendSnailmailRequest_whenDtoIsNull() {
        assertThat(mapper.toSendSnailmailRequest(null)).isNull();
    }

    @Test
    void test_toSendSnailmailRequest() {
        var dto = SnailmailDto.builder()
                .withPersonId("somePersonID")
                .withAttachments(List.of(SnailmailDto.AttachmentDto.builder()
                        .withName("someName")
                        .withContentType("someContentType")
                        .withContent("someContent")
                        .build()))
                .build();

        var request = mapper.toSendSnailmailRequest(dto);

        assertThat(request.getPersonId()).isEqualTo("somePersonID");
        assertThat(request.getAttachments()).hasSize(1);
    }
}
