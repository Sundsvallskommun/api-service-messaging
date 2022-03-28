package se.sundsvall.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class EmailDtoTests {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();

        var attachment = EmailDto.AttachmentDto.builder()
            .withName("attachment name")
            .withContent("content")
            .withContentType("image")
            .build();

        var emailDto = EmailDto.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withAttachments(List.of(attachment))
            .withEmailAddress("mail@mail.com")
            .withHtmlMessage("html message")
            .withSubject("message subject")
            .withMessage("message content")
            .withSenderEmail("mail@mail.com")
            .withSenderName("Sundsvalls kommun")
            .withStatus(MessageStatus.PENDING)
            .build();

        assertThat(emailDto.getBatchId()).isEqualTo(batchId);
        assertThat(emailDto.getMessageId()).isEqualTo(messageId);
        assertThat(emailDto.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(emailDto.getParty().getExternalReferences())
            .hasSameSizeAs(emailDto.getParty().getExternalReferences());
        assertThat(emailDto.getAttachments()).hasSize(1)
            .allSatisfy(attach -> {
                assertThat(attach.getName()).isEqualTo("attachment name");
                assertThat(attach.getContent()).isEqualTo("content");
                assertThat(attach.getContentType()).isEqualTo("image");
            });
        assertThat(emailDto.getEmailAddress()).isEqualTo("mail@mail.com");
        assertThat(emailDto.getHtmlMessage()).isEqualTo("html message");
        assertThat(emailDto.getSubject()).isEqualTo("message subject");
        assertThat(emailDto.getMessage()).isEqualTo("message content");
        assertThat(emailDto.getSenderEmail()).isEqualTo("mail@mail.com");
        assertThat(emailDto.getSenderName()).isEqualTo("Sundsvalls kommun");
        assertThat(emailDto.getStatus()).isEqualTo(MessageStatus.PENDING);
    }
}
