package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import se.sundsvall.messaging.api.model.validation.OneOf;

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
public class LetterRequest extends BatchRequest {

    @Schema(description = "Subject", nullable = true)
    private String subject;

    @NotBlank
    @OneOf({"text/plain", "text/html"})
    @Schema(description = "Content type", allowableValues = {"text/plain", "text/html"})
    private String contentType;

    @NotBlank
    @Schema(description = "Body (BASE64-encoded)")
    private String body;

    @NotBlank
    @Schema(description = "Department and unit that should be billed in case of snailmail",
            example = "SBK" + "(Gatuavdelningen, Trafiksektionen)")
    private String department;

    @Valid
    @Schema(description = "Attachments")
    private List<Attachment> attachments;

    @Getter
    @Setter
    @Builder(setterPrefix = "with")
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(name = "DigitalMailAttachment")
    public static class Attachment {

        @NotNull
        @Schema(name = "delivery mode", description = "Is attachment for Digital or Snail mail?")
        private DeliveryMode deliveryMode;

        @OneOf("application/pdf")
        @Schema(description = "Content type", allowableValues = {"application/pdf"})
        private String contentType;

        @NotBlank
        @Schema(description = "Content (BASE64-encoded)")
        private String content;

        @NotBlank
        @Schema(name = "filename", description = "Filename")
        private String filename;
    }

    public enum DeliveryMode {
        DIGITAL, SNAIL
    }


}
