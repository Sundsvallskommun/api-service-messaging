package se.sundsvall.messaging.dto;

import java.util.List;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

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
    private final Party party;
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
