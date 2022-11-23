package se.sundsvall.messaging.integration.snailmailsender;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import se.sundsvall.messaging.dto.SnailmailDto;

import generated.se.sundsvall.snailmail.Attachment;
import generated.se.sundsvall.snailmail.SendSnailMailRequest;

@Component
public class SnailmailSenderIntegrationMapper {

    SendSnailMailRequest toSendSnailmailRequest(final SnailmailDto dto) {
        if (dto == null) {
            return null;
        }

        List<Attachment> attachments = null;
        if (!CollectionUtils.isEmpty(dto.getAttachments())) {
            attachments = Optional.ofNullable(dto.getAttachments()).stream()
                    .flatMap(Collection::stream)
                    .map(attachment -> new Attachment()
                            .content(attachment.getContent())
                            .contentType(attachment.getContentType())
                            .name(attachment.getName()))
                    .toList();
        }

        return new SendSnailMailRequest()
                .department(dto.getDepartment())
                .deviation(dto.getDeviation())
                .attachments(attachments);
    }
}
