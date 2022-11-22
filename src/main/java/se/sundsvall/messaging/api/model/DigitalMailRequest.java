package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import se.sundsvall.messaging.api.model.validation.OneOf;
import se.sundsvall.messaging.model.Sender;

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
public class DigitalMailRequest extends BatchRequest {

    @Valid
    @Schema(description = "Sender")
    private Sender.DigitalMail sender;

    @Schema(description = "Subject", nullable = true)
    private String subject;

    @NotBlank
    @OneOf({"text/plain", "text/html"})
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
    @Schema(name = "DigitalMailAttachment")
    public static class Attachment {

        @OneOf("application/pdf")
        @Schema(description = "Content type", allowableValues = {"application/pdf"})
        private String contentType;

        @NotBlank
        @Schema(description = "Content (BASE64-encoded)")
        private String content;

        @NotBlank
        @Schema(description = "Filename")
        private String filename;
    }
}
