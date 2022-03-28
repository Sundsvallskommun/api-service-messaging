package se.sundsvall.messaging.integration.emailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class EmailIntegrationMapperTests {

    private final EmailSenderIntegrationMapper mapper = new EmailSenderIntegrationMapper();

    @Test
    void emailDto_toRequest_thenRequestWithSameValues() {
        var attachment = EmailDto.AttachmentDto.builder()
            .withContent("Test")
            .withName("Test")
            .withContentType("Test")
            .build();

        var entity = EmailDto.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withSenderName("Test")
            .withSenderEmail("test@hotmail.com")
            .withEmailAddress("test@hotmail.com")
            .withParty(Party.builder()
                .withPartyId("1")
                .build())
            .withSubject("Test")
            .withMessage("Test")
            .withHtmlMessage("Test")
            .withStatus(MessageStatus.PENDING)
            .withAttachments(List.of(attachment))
            .build();

        var request = mapper.toRequest(entity);

        assertThat(request.getSubject()).isEqualTo(entity.getSubject());
        assertThat(request.getMessage()).isEqualTo(entity.getMessage());
        assertThat(request.getSenderName()).isEqualTo(entity.getSenderName());
        assertThat(request.getSenderEmail()).isEqualTo(entity.getSenderEmail());
        assertThat(request.getHtmlMessage()).isEqualTo(entity.getHtmlMessage());
        assertThat(request.getEmailAddress()).isEqualTo(entity.getEmailAddress());
        assertThat(request.getAttachments()).hasSize(1)
            .allMatch(requestAttachment -> Objects.equals(attachment.getContent(), requestAttachment.getContent()))
            .allMatch(requestAttachment -> Objects.equals(attachment.getName(), requestAttachment.getName()))
            .allMatch(requestAttachment -> Objects.equals(attachment.getContentType(), requestAttachment.getContentType()));
    }

    @Test
    void emailDto_toRequest_thenNullRequest() {
        assertThat(mapper.toRequest(null)).isNull();
    }
}
