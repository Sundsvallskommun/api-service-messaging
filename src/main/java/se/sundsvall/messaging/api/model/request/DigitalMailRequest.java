package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.model.request.validation.OneOf;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record DigitalMailRequest(

        @Valid
        @NotNull
        @Schema(description = "Party", requiredMode = REQUIRED)
        Party party,

        @Schema(description = "Headers")
        List<@Valid Header> headers,

        @Valid
        @Schema(description = "Sender")
        Sender sender,

        @Schema(description = "Subject", nullable = true)
        String subject,

        @NotBlank
        @OneOf({"text/plain", "text/html"})
        @Schema(description = "Content type", allowableValues = {"text/plain", "text/html"})
        String contentType,

        @NotBlank
        @Schema(description = "Body (plain text if contentType is set to 'text/plain', BASE64-encoded if contentType is set to 'application/html')")
        String body,

        @Schema(description = "Attachments")
        List<@Valid Attachment> attachments) {

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "DigitalMailParty")
    public record Party(

        @NotEmpty
        @ArraySchema(schema = @Schema(description = "The message party ids", format = "uuid"), minItems = 1)
        List<@ValidUuid String> partyIds,

        @Schema(description = "External references")
        List<@Valid ExternalReference> externalReferences) { }

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "DigitalMailSender")
    public record Sender(

        @Valid
        @NotNull
        SupportInfo supportInfo) {

        @With
        @Builder(setterPrefix = "with")
        @Schema(name = "DigitalMailSenderSupportInfo")
        public record SupportInfo(

            @NotBlank
            String text,

            @javax.validation.constraints.Email
            @NotBlank
            String emailAddress,

            @NotBlank
            String phoneNumber,

            @NotBlank
            String url) { }
    }

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "DigitalMailAttachment")
    public record Attachment(

        @OneOf("application/pdf")
        @Schema(description = "Content type", allowableValues = {"application/pdf"})
        String contentType,

        @NotBlank
        @Schema(description = "Content (BASE64-encoded)")
        String content,

        @NotBlank
        @Schema(description = "Filename")
        String filename) { }
}
