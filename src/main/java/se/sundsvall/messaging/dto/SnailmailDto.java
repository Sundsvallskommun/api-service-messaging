package se.sundsvall.messaging.dto;


import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnailmailDto {

    private final String department;
    private final String deviation;
    private final String personId;
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
