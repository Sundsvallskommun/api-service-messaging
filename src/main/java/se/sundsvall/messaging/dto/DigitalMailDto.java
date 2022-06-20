package se.sundsvall.messaging.dto;

import java.util.List;

import se.sundsvall.messaging.model.ContentType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalMailDto {

    private final String partyId;
    private final String subject;
    private final ContentType contentType;
    private final String body;
    private final List<AttachmentDto> attachments;

    @Getter
    @Builder(setterPrefix = "with")
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AttachmentDto {

        private final ContentType contentType;
        private final String content;
        private final String filename;
    }
}
