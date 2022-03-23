package se.sundsvall.messaging.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;

class EmailEntityTest {

    @Test
    void testBuilderAndGetters() {
        String id = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        EmailEntity.Attachment attachment = EmailEntity.Attachment.builder()
                .withId(id)
                .withName("attachment name")
                .withContent("content")
                .withContentType("image")
                .build();

        EmailEntity email = EmailEntity.builder()
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
                .withCreatedAt(dateTimeNow)
                .build();

        assertThat(email.getBatchId()).isEqualTo(batchId);
        assertThat(email.getMessageId()).isEqualTo(messageId);
        assertThat(email.getPartyId()).isEqualTo(partyId);
        assertThat(email.getAttachments()).hasSize(1)
                .allSatisfy(attach -> {
                    assertThat(attach.getId()).isEqualTo(id);
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
        assertThat(email.getCreatedAt()).isEqualTo(dateTimeNow);
    }

    @Test
    void testSetters() {
        String id = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        EmailEntity.Attachment attachment = new EmailEntity.Attachment();
        attachment.setId(id);
        attachment.setName("attachment name");
        attachment.setContent("content");
        attachment.setContentType("image");

        EmailEntity email = new EmailEntity();
        email.setBatchId(batchId);
        email.setMessageId(messageId);
        email.setPartyId(partyId);
        email.setAttachments(List.of(attachment));
        email.setEmailAddress("mail@mail.com");
        email.setHtmlMessage("html message");
        email.setSubject("message subject");
        email.setMessage("message content");
        email.setSenderEmail("mail@mail.com");
        email.setSenderName("Sundsvalls kommun");
        email.setStatus(MessageStatus.PENDING);
        email.setCreatedAt(dateTimeNow);

        assertThat(email.getBatchId()).isEqualTo(batchId);
        assertThat(email.getMessageId()).isEqualTo(messageId);
        assertThat(email.getPartyId()).isEqualTo(partyId);
        assertThat(email.getAttachments()).hasSize(1)
                .allSatisfy(attach -> {
                    assertThat(attach.getId()).isEqualTo(id);
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
        assertThat(email.getCreatedAt()).isEqualTo(dateTimeNow);
    }

}
