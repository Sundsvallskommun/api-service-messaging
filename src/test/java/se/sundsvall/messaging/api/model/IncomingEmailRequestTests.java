package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

class IncomingEmailRequestTests {

    @Test
    void testBuilderAndGetters() {
        var partyId = UUID.randomUUID().toString();

        var attachment = EmailRequest.Attachment.builder()
            .withContent("content")
            .withContentType("image")
            .withName("name")
            .build();

        var request = EmailRequest.builder()
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withSubject("subject")
            .withMessage("message")
            .withHtmlMessage("html message")
            .withSenderName("Sundsvalls kommun")
            .withSenderEmail("noreply@sundsvall.se")
            .withEmailAddress("john.doe@example.com")
            .withAttachments(List.of(attachment))
            .build();

        assertThat(request.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(request.getParty().getExternalReferences()).hasSize(1);
        assertThat(request.getSubject()).isEqualTo("subject");
        assertThat(request.getMessage()).isEqualTo("message");
        assertThat(request.getHtmlMessage()).isEqualTo("html message");
        assertThat(request.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(request.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(request.getEmailAddress()).isEqualTo("john.doe@example.com");
        assertThat(request.getAttachments()).hasSize(1).allSatisfy(attach -> {
            assertThat(attach.getContent()).isEqualTo("content");
            assertThat(attach.getContentType()).isEqualTo("image");
            assertThat(attach.getName()).isEqualTo("name");
        });
    }

    @Test
    void testSetters() {
        var partyId = UUID.randomUUID().toString();

        var attachment = new EmailRequest.Attachment();
        attachment.setContent("content");
        attachment.setContentType("image");
        attachment.setName("name");

        var party = new Party();
        party.setPartyId(partyId);
        party.setExternalReferences(List.of(new ExternalReference()));

        var request = new EmailRequest();
        request.setParty(party);
        request.setSubject("subject");
        request.setMessage("message");
        request.setHtmlMessage("html message");
        request.setSenderName("Sundsvalls kommun");
        request.setSenderEmail("noreply@sundsvall.se");
        request.setEmailAddress("john.doe@example.com");
        request.setAttachments(List.of(attachment));

        assertThat(request.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(request.getParty().getExternalReferences()).hasSize(1);
        assertThat(request.getSubject()).isEqualTo("subject");
        assertThat(request.getMessage()).isEqualTo("message");
        assertThat(request.getHtmlMessage()).isEqualTo("html message");
        assertThat(request.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(request.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(request.getEmailAddress()).isEqualTo("john.doe@example.com");

        assertThat(request.getAttachments()).hasSize(1).allSatisfy(attach -> {
            assertThat(attach.getContent()).isEqualTo("content");
            assertThat(attach.getContentType()).isEqualTo("image");
            assertThat(attach.getName()).isEqualTo("name");
        });
    }
}
