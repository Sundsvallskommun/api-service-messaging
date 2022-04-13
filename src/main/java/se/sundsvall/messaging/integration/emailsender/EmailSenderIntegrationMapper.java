package se.sundsvall.messaging.integration.emailsender;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.EmailDto;

import generated.se.sundsvall.emailsender.Attachment;
import generated.se.sundsvall.emailsender.SendEmailRequest;
import generated.se.sundsvall.emailsender.Sender;

@Component
class EmailSenderIntegrationMapper {

    SendEmailRequest toSendEmailRequest(final EmailDto emailDto) {
        if (emailDto == null) {
            return null;
        }

        var attachments = Optional.ofNullable(emailDto.getAttachments()).stream()
            .flatMap(Collection::stream)
            .map(attachment -> new Attachment()
                .content(attachment.getContent())
                .contentType(attachment.getContentType())
                .name(attachment.getName()))
            .toList();

        return new SendEmailRequest()
            .subject(emailDto.getSubject())
            .message(emailDto.getMessage())
            .emailAddress(emailDto.getEmailAddress())
            .sender(new Sender()
                .name(emailDto.getSender().getName())
                .address(emailDto.getSender().getAddress())
                .replyTo(emailDto.getSender().getReplyTo()))
            .htmlMessage(emailDto.getHtmlMessage())
            .attachments(attachments);
    }
}
