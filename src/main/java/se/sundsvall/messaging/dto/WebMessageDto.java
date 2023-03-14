package se.sundsvall.messaging.dto;

import java.util.List;

import se.sundsvall.messaging.model.PartyWithRequiredPartyId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebMessageDto {

    private PartyWithRequiredPartyId party;
    private String message;
    private final List<AttachmentDto> attachments;

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class AttachmentDto {

        private final String fileName;
        private final String base64Data;
        private final String mimeType;
    }
}
