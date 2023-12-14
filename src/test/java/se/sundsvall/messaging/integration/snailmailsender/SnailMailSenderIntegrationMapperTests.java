package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.model.request.EnvelopeType;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.snailmail.Attachment;

@UnitTest
class SnailMailSenderIntegrationMapperTests {

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
                .withContentType("someContentType")
                .withContent("someContent")
                .withEnvelopeType(EnvelopeType.PLAIN)
                .build()))
            .build();

        var mappedRequest = mapper.toSendSnailmailRequest(dto);

        assertThat(mappedRequest.getAttachments()).hasSize(1);
        assertThat(mappedRequest.getAttachments().get(0).getName()).isEqualTo("someName");
        assertThat(mappedRequest.getAttachments().get(0).getContentType()).isEqualTo("someContentType");
        assertThat(mappedRequest.getAttachments().get(0).getContent()).isEqualTo("someContent");
        assertThat(mappedRequest.getAttachments().get(0).getEnvelopeType()).isEqualTo(Attachment.EnvelopeTypeEnum.PLAIN);
    }

    @Test
    void test_toSendSnailMailRequestWithEmptyEnvelopeType() {
        var dto = SnailMailDto.builder()
                .withAttachments(List.of(SnailMailDto.Attachment.builder()
                        .withName("someName")
                        .withContentType("someContentType")
                        .withContent("someContent")
                        .build()))
                .build();

        var mappedRequest = mapper.toSendSnailmailRequest(dto);

        assertThat(mappedRequest.getAttachments()).hasSize(1);
        assertThat(mappedRequest.getAttachments().get(0).getName()).isEqualTo("someName");
        assertThat(mappedRequest.getAttachments().get(0).getContentType()).isEqualTo("someContentType");
        assertThat(mappedRequest.getAttachments().get(0).getContent()).isEqualTo("someContent");
        assertThat(mappedRequest.getAttachments().get(0).getEnvelopeType()).isNull();
    }
}
