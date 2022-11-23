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
    @Schema(description = "Body (plain text if contentType is set to 'text/plain', BASE64-encoded if contentType is set to 'text/html')")
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
    @Schema(name = "LetterAttachment")
    public static class Attachment {

        @NotNull
        @Schema(description = "Delivery mode")
        private DeliveryMode deliveryMode;

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

    @Schema(description = """
        Delivery mode, to indicate whether an attachment is intended/allowed to be used for 
        digital mail, snail-mail or both
        """
    )
    public enum DeliveryMode {
        BOTH,
        DIGITAL_MAIL,
        SNAIL_MAIL
    }
}
