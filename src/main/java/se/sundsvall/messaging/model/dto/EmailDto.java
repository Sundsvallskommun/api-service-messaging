package se.sundsvall.messaging.model.dto;

import java.util.List;

import se.sundsvall.messaging.api.MessageStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder(setterPrefix = "with")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailDto {

    private final String batchId;
    private final String messageId;
    private final String partyId;
    private final String emailAddress;
    private final String subject;
    private final String message;
    private final String htmlMessage;
    private final String senderName;
    private final String senderEmail;
    private final List<AttachmentDto> attachments;
    private final MessageStatus status;

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class AttachmentDto {

        private final String content;
        private final String name;
        private final String contentType;
    }
}
