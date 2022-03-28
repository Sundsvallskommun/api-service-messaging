package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class EmailMapperTests {

    @Test
    void emailRequest_toEntity_returnsNullWhenRequestIsNull() {
        assertThat(EmailMapper.toEntity(null)).isNull();
    }

    @Test
    void emailRequest_toEntity_hasSameValues() {
        var attachment = IncomingEmailRequest.Attachment.builder()
            .withContent("Test")
            .withName("Test")
            .withContentType("Test")
            .build();

        var emailRequest = IncomingEmailRequest.builder()
            .withSenderName("Test")
            .withSenderEmail("test@hotmail.com")
            .withEmailAddress("test@hotmail.com")
            .withParty(Party.builder()
                .withPartyId("1")
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()))
                .build())
            .withSubject("Test")
            .withMessage("Test")
            .withHtmlMessage("Test")
            .withAttachments(List.of(attachment))
            .build();

        EmailEntity emailEntity = EmailMapper.toEntity(emailRequest);

        assertThat(emailEntity.getSenderName()).isEqualTo(emailEntity.getSenderName());
        assertThat(emailEntity.getSenderEmail()).isEqualTo(emailRequest.getSenderEmail());
        assertThat(emailEntity.getEmailAddress()).isEqualTo(emailRequest.getEmailAddress());
        assertThat(emailEntity.getPartyId()).isEqualTo(emailRequest.getParty().getPartyId());
        assertThat(emailEntity.getExternalReferences()).hasSize(1);
        assertThat(emailEntity.getSubject()).isEqualTo(emailRequest.getSubject());
        assertThat(emailEntity.getMessage()).isEqualTo(emailRequest.getMessage());
        assertThat(emailEntity.getHtmlMessage()).isEqualTo(emailRequest.getHtmlMessage());
        assertThat(emailEntity.getAttachments())
                .extracting(EmailEntity.Attachment::getId)
                .isNotNull();
    }

    @Test
    void emailRequest_toEntity_receivesBatchIdAndMessageId() {
        var emailRequest = IncomingEmailRequest.builder().build();

        var emailEntity = EmailMapper.toEntity(emailRequest);

        assertThat(emailEntity.getBatchId()).isNotNull();
        assertThat(emailEntity.getMessageId()).isNotNull();
    }

    @Test
    void emailEntity_toDto_returnsNullWhenEntityIsNull() {
        assertThat(EmailMapper.toDto(null)).isNull();
    }

    @Test
    void emailEntity_toDto_hasSameValues() {
        var attachment = EmailEntity.Attachment.builder()
            .withContent("Test")
            .withName("Test")
            .withContentType("Test")
            .build();

        var emailEntity = EmailEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withSenderName("Test")
            .withSenderEmail("test@hotmail.com")
            .withEmailAddress("test@hotmail.com")
            .withPartyId("1")
            .withExternalReferences(Map.of("key", "value"))
            .withSubject("Test")
            .withMessage("Test")
            .withHtmlMessage("Test")
            .withStatus(MessageStatus.PENDING)
            .withAttachments(List.of(attachment))
            .withCreatedAt(LocalDateTime.now())
            .build();

        var emailDto = EmailMapper.toDto(emailEntity);

        assertThat(emailDto.getBatchId()).isEqualTo(emailEntity.getBatchId());
        assertThat(emailDto.getMessageId()).isEqualTo(emailEntity.getMessageId());
        assertThat(emailDto.getSenderName()).isEqualTo(emailDto.getSenderName());
        assertThat(emailDto.getSenderEmail()).isEqualTo(emailDto.getSenderEmail());
        assertThat(emailDto.getEmailAddress()).isEqualTo(emailEntity.getEmailAddress());
        assertThat(emailDto.getParty().getPartyId()).isEqualTo(emailEntity.getPartyId());
        assertThat(emailDto.getParty().getExternalReferences()).hasSize(1);
        assertThat(emailDto.getSubject()).isEqualTo(emailEntity.getSubject());
        assertThat(emailDto.getMessage()).isEqualTo(emailEntity.getMessage());
        assertThat(emailDto.getHtmlMessage()).isEqualTo(emailEntity.getHtmlMessage());
        assertThat(emailDto.getStatus()).isEqualTo(emailEntity.getStatus());
        assertThat(emailDto.getAttachments()).hasSize(1)
            .allMatch(attachmentDto -> Objects.equals(attachment.getContent(), attachmentDto.getContent()))
            .allMatch(attachmentDto -> Objects.equals(attachment.getName(), attachmentDto.getName()))
            .allMatch(attachmentDto -> Objects.equals(attachment.getContentType(), attachmentDto.getContentType()));
    }
}
