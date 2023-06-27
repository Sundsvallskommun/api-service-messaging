package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record EmailRequest(

        @Valid
        @Schema(description = "Party")
        Party party,

        @Email
        @NotBlank
        @Schema(description = "Recipient e-mail address", requiredMode = REQUIRED)
        String emailAddress,

        @NotBlank
        @Schema(description = "E-mail subject", requiredMode = REQUIRED)
        String subject,

        @Schema(description = "E-mail plain-text body")
        String message,

        @ValidBase64(nullable = true)
        @Schema(description = "E-mail HTML body (BASE64-encoded)")
        String htmlMessage,

        @Valid
        @Schema(description = "Sender")
        Sender sender,

        @ArraySchema(schema = @Schema(implementation = Attachment.class))
        List<@Valid Attachment> attachments) {

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "EmailRequestParty")
    public record Party(

        @ValidUuid(nullable = true)
        @Schema(description = "The message party id", format = "uuid")
        String partyId,

        @Schema(description = "External references")
        List<@Valid ExternalReference> externalReferences) {
    }

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "EmailSender", description = "Attachment")
    public record Sender(

        @NotBlank
        @Schema(description = "The sender of the e-mail")
        String name,

        @Email
        @NotBlank
        @Schema(description = "Sender e-mail address", example = "sender@sender.se")
        String address,

        @Email
        @Schema(description = "Reply-to e-mail address", example = "sender@sender.se")
        String replyTo) { }

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "EmailAttachment", description = "Attachment")
    public record Attachment(

        @NotBlank
        @Schema(description = "The attachment filename", example = "test.txt", requiredMode = REQUIRED)
        String name,

        @Schema(description = "The attachment content type", example = "text/plain")
        String contentType,

        @ValidBase64
        @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED)
        String content) { }
}