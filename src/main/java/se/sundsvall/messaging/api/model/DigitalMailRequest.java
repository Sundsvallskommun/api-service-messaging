package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.model.validation.In;
import se.sundsvall.messaging.model.ExternalReference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalMailRequest extends Request {

    @Schema(description = "Subject", nullable = true)
    private String subject;

    @NotBlank
    @In({"text/plain", "text/html"})
    @Schema(description = "Content type", allowableValues = {"text/plain", "text/html"})
    private String contentType;

    @NotBlank
    @Schema(description = "Body (BASE64-encoded)")
    private String body;

    @Valid
    @Schema(description = "Attachments")
    private List<Attachment> attachments;

    @Getter
    @Setter
    @Builder(setterPrefix = "with")
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(name = "DigitalMailParty")
    public static class Party {

        @Schema(description = "The message party ID:s", example = "[f427952b-247c-4d3b-b081-675a467b3619]")
        private List<@ValidUuid String> partyIds;

        @Schema(description = "External references")
        private List<@Valid ExternalReference> externalReferences;
    }

    @Getter
    @Setter
    @Builder(setterPrefix = "with")
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(name = "DigitalMailAttachment")
    public static class Attachment {

        @In("application/pdf")
        @Schema(description = "Content type", allowableValues = {"application/pdf"})
        private String contentType;

        @NotBlank
        @Schema(description = "Content (BASE64-encoded)")
        private String content;

        @NotBlank
        @Schema(name = "filename", description = "Filename")
        private String filename;
    }
}
