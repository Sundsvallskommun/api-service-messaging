package se.sundsvall.messaging.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;

class EmailDtoTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();

        EmailDto.AttachmentDto attachment = EmailDto.AttachmentDto.builder()
                .withName("attachment name")
                .withContent("content")
                .withContentType("image")
                .build();

        EmailDto email = EmailDto.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withAttachments(List.of(attachment))
                .withEmailAddress("mail@mail.com")
                .withHtmlMessage("html message")
                .withSubject("message subject")
                .withMessage("message content")
                .withSenderEmail("mail@mail.com")
                .withSenderName("Sundsvalls kommun")
                .withStatus(MessageStatus.PENDING)
                .build();

        assertThat(email.getBatchId()).isEqualTo(batchId);
        assertThat(email.getMessageId()).isEqualTo(messageId);
        assertThat(email.getPartyId()).isEqualTo(partyId);
        assertThat(email.getAttachments()).hasSize(1)
                .allSatisfy(attach -> {
                    assertThat(attach.getName()).isEqualTo("attachment name");
                    assertThat(attach.getContent()).isEqualTo("content");
                    assertThat(attach.getContentType()).isEqualTo("image");
                });
        assertThat(email.getEmailAddress()).isEqualTo("mail@mail.com");
        assertThat(email.getHtmlMessage()).isEqualTo("html message");
        assertThat(email.getSubject()).isEqualTo("message subject");
        assertThat(email.getMessage()).isEqualTo("message content");
        assertThat(email.getSenderEmail()).isEqualTo("mail@mail.com");
        assertThat(email.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(email.getStatus()).isEqualTo(MessageStatus.PENDING);
    }

}
