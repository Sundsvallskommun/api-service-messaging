package se.sundsvall.messaging.integration.snailmailsender;


import generated.se.sundsvall.snailmail.Attachment;
import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.dto.SnailmailDto;

import java.util.Collection;
import java.util.Optional;

@Component
public class SnailmailSenderIntegrationMapper {

    SendSnailMailRequest toSendSnailmailRequest(final SnailmailDto dto) {
        if (dto == null) {
            return null;
        }

        var attachments = Optional.ofNullable(dto.getAttachments()).stream()
                .flatMap(Collection::stream)
                .map(attachment -> new Attachment()
                        .content(attachment.getContent())
                        .contentType(attachment.getContentType())
                        .name(attachment.getName()))
                .toList();

        return new SendSnailMailRequest()
                .personId(dto.getPersonId())
                .attachments(attachments);
    }
}
