package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.model.dto.EmailDto;
import se.sundsvall.messaging.model.entity.EmailEntity;

import generated.se.sundsvall.emailsender.EmailRequest;

class EmailMapperTest {

    @Test
    void emailRequest_toEntity_returnsNullWhenRequestIsNull() {
        assertThat(EmailMapper.toEntity(null)).isNull();
    }


    @Test
    void emailRequest_toEntity_hasSameValues() {
        IncomingEmailRequest.Attachment attachment = IncomingEmailRequest.Attachment.builder()
                .withContent("Test")
                .withName("Test")
                .withContentType("Test")
                .build();

        IncomingEmailRequest emailRequest = IncomingEmailRequest.builder()
                .withSenderName("Test")
                .withSenderEmail("test@hotmail.com")
                .withEmailAddress("test@hotmail.com")
                .withPartyId("1")
                .withSubject("Test")
                .withMessage("Test")
                .withHtmlMessage("Test")
                .withAttachments(List.of(attachment))
                .build();

        EmailEntity emailEntity = EmailMapper.toEntity(emailRequest);

        assertThat(emailEntity.getSenderName()).isEqualTo(emailEntity.getSenderName());
        assertThat(emailEntity.getSenderEmail()).isEqualTo(emailRequest.getSenderEmail());
        assertThat(emailEntity.getEmailAddress()).isEqualTo(emailRequest.getEmailAddress());
        assertThat(emailEntity.getPartyId()).isEqualTo(emailRequest.getPartyId());
        assertThat(emailEntity.getSubject()).isEqualTo(emailRequest.getSubject());
        assertThat(emailEntity.getMessage()).isEqualTo(emailRequest.getMessage());
        assertThat(emailEntity.getHtmlMessage()).isEqualTo(emailRequest.getHtmlMessage());
        assertThat(emailEntity.getAttachments())
                .extracting(EmailEntity.Attachment::getId)
                .isNotNull();
    }

    @Test
    void emailRequest_toEntity_receivesBatchIdAndMessageId() {
        IncomingEmailRequest emailRequest = IncomingEmailRequest.builder().build();

        EmailEntity emailEntity = EmailMapper.toEntity(emailRequest);

        assertThat(emailEntity.getBatchId()).isNotNull();
        assertThat(emailEntity.getMessageId()).isNotNull();
    }

    @Test
    void emailEntity_toDto_returnsNullWhenEntityIsNull() {
        assertThat(EmailMapper.toDto(null)).isNull();
    }

    @Test
    void emailEntity_toDto_hasSameValues() {
        EmailEntity.Attachment attachment = EmailEntity.Attachment.builder()
                .withContent("Test")
                .withName("Test")
                .withContentType("Test")
                .build();

        EmailEntity emailEntity = EmailEntity.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withSenderName("Test")
                .withSenderEmail("test@hotmail.com")
                .withEmailAddress("test@hotmail.com")
                .withPartyId("1")
                .withSubject("Test")
                .withMessage("Test")
                .withHtmlMessage("Test")
                .withStatus(MessageStatus.PENDING)
                .withAttachments(List.of(attachment))
                .withCreatedAt(LocalDateTime.now())
                .build();

        EmailDto dto = EmailMapper.toDto(emailEntity);

        assertThat(dto.getBatchId()).isEqualTo(emailEntity.getBatchId());
        assertThat(dto.getMessageId()).isEqualTo(emailEntity.getMessageId());
        assertThat(dto.getSenderName()).isEqualTo(dto.getSenderName());
        assertThat(dto.getSenderEmail()).isEqualTo(dto.getSenderEmail());
        assertThat(dto.getEmailAddress()).isEqualTo(emailEntity.getEmailAddress());
        assertThat(dto.getPartyId()).isEqualTo(emailEntity.getPartyId());
        assertThat(dto.getSubject()).isEqualTo(emailEntity.getSubject());
        assertThat(dto.getMessage()).isEqualTo(emailEntity.getMessage());
        assertThat(dto.getHtmlMessage()).isEqualTo(emailEntity.getHtmlMessage());
        assertThat(dto.getStatus()).isEqualTo(emailEntity.getStatus());
        assertThat(dto.getAttachments()).hasSize(1)
                .allMatch(attachmentDto -> Objects.equals(attachment.getContent(), attachmentDto.getContent()))
                .allMatch(attachmentDto -> Objects.equals(attachment.getName(), attachmentDto.getName()))
                .allMatch(attachmentDto -> Objects.equals(attachment.getContentType(), attachmentDto.getContentType()));
    }

    @Test
    void emailEntity_toRequest_thenRequestWithSameValues() {
        EmailEntity.Attachment attachment = EmailEntity.Attachment.builder()
                .withContent("Test")
                .withName("Test")
                .withContentType("Test")
                .build();

        EmailEntity entity = EmailEntity.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withSenderName("Test")
                .withSenderEmail("test@hotmail.com")
                .withEmailAddress("test@hotmail.com")
                .withPartyId("1")
                .withSubject("Test")
                .withMessage("Test")
                .withHtmlMessage("Test")
                .withStatus(MessageStatus.PENDING)
                .withCreatedAt(LocalDateTime.now())
                .withAttachments(List.of(attachment))
                .build();

        EmailRequest request = EmailMapper.toRequest(entity);

        assertThat(request.getSubject()).isEqualTo(entity.getSubject());
        assertThat(request.getMessage()).isEqualTo(entity.getMessage());
        assertThat(request.getSenderName()).isEqualTo(entity.getSenderName());
        assertThat(request.getSenderEmail()).isEqualTo(entity.getSenderEmail());
        assertThat(request.getHtmlMessage()).isEqualTo(entity.getHtmlMessage());
        assertThat(request.getEmailAddress()).isEqualTo(entity.getEmailAddress());
        assertThat(request.getAttachments()).hasSize(1)
                .allMatch(attachmentRequest -> Objects.equals(attachment.getContent(), attachmentRequest.getContent()))
                .allMatch(attachmentRequest -> Objects.equals(attachment.getName(), attachmentRequest.getName()))
                .allMatch(attachmentRequest -> Objects.equals(attachment.getContentType(), attachmentRequest.getContentType()));
    }

    @Test
    void emailEntity_toRequest_thenNullRequest() {
        assertThat(EmailMapper.toRequest(null)).isNull();
    }
}
