package se.sundsvall.messaging.integration.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;

class EmailEntityTests {

    @Test
    void testBuilderAndGetters() {
        var id = UUID.randomUUID().toString();
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var attachment = EmailEntity.Attachment.builder()
            .withId(id)
            .withName("attachment name")
            .withContent("content")
            .withContentType("image")
            .build();

        var emailEntity = EmailEntity.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withPartyId(partyId)
            .withExternalReferences(Map.of("key", "value"))
            .withAttachments(List.of(attachment))
            .withEmailAddress("mail@mail.com")
            .withHtmlMessage("html message")
            .withSubject("message subject")
            .withMessage("message content")
            .withSenderEmail("mail@mail.com")
            .withSenderName("Sundsvalls kommun")
            .withStatus(MessageStatus.PENDING)
            .withCreatedAt(now)
            .build();

        assertThat(emailEntity.getBatchId()).isEqualTo(batchId);
        assertThat(emailEntity.getMessageId()).isEqualTo(messageId);
        assertThat(emailEntity.getPartyId()).isEqualTo(partyId);
        assertThat(emailEntity.getExternalReferences()).hasSize(1);
        assertThat(emailEntity.getAttachments()).hasSize(1)
            .allSatisfy(attach -> {
                assertThat(attach.getId()).isEqualTo(id);
                assertThat(attach.getName()).isEqualTo("attachment name");
                assertThat(attach.getContent()).isEqualTo("content");
                assertThat(attach.getContentType()).isEqualTo("image");
            });
        assertThat(emailEntity.getEmailAddress()).isEqualTo("mail@mail.com");
        assertThat(emailEntity.getHtmlMessage()).isEqualTo("html message");
        assertThat(emailEntity.getSubject()).isEqualTo("message subject");
        assertThat(emailEntity.getMessage()).isEqualTo("message content");
        assertThat(emailEntity.getSenderEmail()).isEqualTo("mail@mail.com");
        assertThat(emailEntity.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(emailEntity.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(emailEntity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testSetters() {
        var id = UUID.randomUUID().toString();
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var attachment = new EmailEntity.Attachment();
        attachment.setId(id);
        attachment.setName("attachment name");
        attachment.setContent("content");
        attachment.setContentType("image");

        var emailEntity = new EmailEntity();
        emailEntity.setBatchId(batchId);
        emailEntity.setMessageId(messageId);
        emailEntity.setPartyId(partyId);
        emailEntity.setExternalReferences(Map.of("key", "value"));
        emailEntity.setAttachments(List.of(attachment));
        emailEntity.setEmailAddress("mail@mail.com");
        emailEntity.setHtmlMessage("html message");
        emailEntity.setSubject("message subject");
        emailEntity.setMessage("message content");
        emailEntity.setSenderEmail("mail@mail.com");
        emailEntity.setSenderName("Sundsvalls kommun");
        emailEntity.setStatus(MessageStatus.PENDING);
        emailEntity.setCreatedAt(now);

        assertThat(emailEntity.getBatchId()).isEqualTo(batchId);
        assertThat(emailEntity.getMessageId()).isEqualTo(messageId);
        assertThat(emailEntity.getPartyId()).isEqualTo(partyId);
        assertThat(emailEntity.getExternalReferences()).hasSize(1);
        assertThat(emailEntity.getAttachments()).hasSize(1)
            .allSatisfy(attach -> {
                assertThat(attach.getId()).isEqualTo(id);
                assertThat(attach.getName()).isEqualTo("attachment name");
                assertThat(attach.getContent()).isEqualTo("content");
                assertThat(attach.getContentType()).isEqualTo("image");
            });
        assertThat(emailEntity.getEmailAddress()).isEqualTo("mail@mail.com");
        assertThat(emailEntity.getHtmlMessage()).isEqualTo("html message");
        assertThat(emailEntity.getSubject()).isEqualTo("message subject");
        assertThat(emailEntity.getMessage()).isEqualTo("message content");
        assertThat(emailEntity.getSenderEmail()).isEqualTo("mail@mail.com");
        assertThat(emailEntity.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(emailEntity.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(emailEntity.getCreatedAt()).isEqualTo(now);
    }
}
