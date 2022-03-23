package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class IncomingEmailRequestTest {

    @Test
    void testBuilderAndGetters() {
        String partyId = UUID.randomUUID().toString();

        IncomingEmailRequest.Attachment attachment = IncomingEmailRequest.Attachment.builder()
                .withContent("content")
                .withContentType("image")
                .withName("name")
                .build();

        IncomingEmailRequest emailRequest = IncomingEmailRequest.builder()
                .withPartyId(partyId)
                .withSubject("subject")
                .withMessage("message")
                .withHtmlMessage("html message")
                .withSenderName("Sundsvalls kommun")
                .withSenderEmail("noreply@sundsvall.se")
                .withEmailAddress("john.doe@example.com")
                .withAttachments(List.of(attachment))
                .build();

        assertThat(emailRequest.getPartyId()).isEqualTo(partyId);
        assertThat(emailRequest.getSubject()).isEqualTo("subject");
        assertThat(emailRequest.getMessage()).isEqualTo("message");
        assertThat(emailRequest.getHtmlMessage()).isEqualTo("html message");
        assertThat(emailRequest.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(emailRequest.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(emailRequest.getEmailAddress()).isEqualTo("john.doe@example.com");
        assertThat(emailRequest.getAttachments()).hasSize(1)
                .allSatisfy(attach -> {
                    assertThat(attach.getContent()).isEqualTo("content");
                    assertThat(attach.getContentType()).isEqualTo("image");
                    assertThat(attach.getName()).isEqualTo("name");
                });
    }

    @Test
    void testSetters() {
        String partyId = UUID.randomUUID().toString();

        IncomingEmailRequest.Attachment attachment = new IncomingEmailRequest.Attachment();
        attachment.setContent("content");
        attachment.setContentType("image");
        attachment.setName("name");

        IncomingEmailRequest emailRequest = new IncomingEmailRequest();
        emailRequest.setPartyId(partyId);
        emailRequest.setSubject("subject");
        emailRequest.setMessage("message");
        emailRequest.setHtmlMessage("html message");
        emailRequest.setSenderName("Sundsvalls kommun");
        emailRequest.setSenderEmail("noreply@sundsvall.se");
        emailRequest.setEmailAddress("john.doe@example.com");
        emailRequest.setAttachments(List.of(attachment));

        assertThat(emailRequest.getPartyId()).isEqualTo(partyId);
        assertThat(emailRequest.getSubject()).isEqualTo("subject");
        assertThat(emailRequest.getMessage()).isEqualTo("message");
        assertThat(emailRequest.getHtmlMessage()).isEqualTo("html message");
        assertThat(emailRequest.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(emailRequest.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(emailRequest.getEmailAddress()).isEqualTo("john.doe@example.com");
        // Just for coverage
        assertThat(emailRequest.toString()).isNotNull();
        assertThat(emailRequest.getAttachments()).hasSize(1)
                .allSatisfy(attach -> {
                    assertThat(attach.getContent()).isEqualTo("content");
                    assertThat(attach.getContentType()).isEqualTo("image");
                    assertThat(attach.getName()).isEqualTo("name");
                    // Just for coverage
                    assertThat(attach.toString()).isNotNull();
                });
    }
}
