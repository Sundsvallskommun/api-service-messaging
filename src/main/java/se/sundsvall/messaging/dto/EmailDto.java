package se.sundsvall.messaging.dto;

import java.util.List;

import se.sundsvall.messaging.model.Sender;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailDto {

    private final Sender.Email sender;
    private final String emailAddress;
    private final String subject;
    private final String message;
    private final String htmlMessage;
    private final List<AttachmentDto> attachments;

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class AttachmentDto {

        private final String content;
        private final String name;
        private final String contentType;
    }
}
