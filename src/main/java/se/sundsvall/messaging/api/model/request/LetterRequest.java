package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public record LetterRequest(

        @Valid
        @NotNull
        @Schema(description = "Party", requiredMode = REQUIRED)
        Party party,

        @Schema(description = "Headers")
        List<@Valid Header> headers,

        @Schema(description = "Subject", requiredMode = REQUIRED)
        String subject,

        @Valid
        @Schema(description = "Sender")
        Sender sender,

        @NotBlank
        @OneOf({"text/plain", "text/html"})
        @Schema(description = "Content type", allowableValues = {"text/plain", "text/html"})
        String contentType,

        @NotBlank
        @Schema(description = "Body (plain text if contentType is set to 'text/plain', BASE64-encoded if contentType is set to 'text/html')")
        String body,

        @NotBlank
        @Schema(description = "Department and unit that should be billed in case of snailmail",
            example = "SBK" + "(Gatuavdelningen, Trafiksektionen)")
        String department,

        @Schema(description = "If the letter to send deviates from the standard", example = "A3 Ritning")
        String deviation,

        @NotEmpty
        @ArraySchema(schema = @Schema(description = "Attachments"), minItems = 1)
        List<@Valid Attachment> attachments) {

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "LetterParty")
    public record Party(

        @NotEmpty
        @ArraySchema(schema = @Schema(description = "The message party ids", format = "uuid"), minItems = 1)
        List<@ValidUuid String> partyIds,

        @Schema(description = "External references")
        List<@Valid ExternalReference> externalReferences) { }

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "LetterSender")
    public record Sender(

            @Valid
            @NotNull
            SupportInfo supportInfo) {

        @With
        @Builder(setterPrefix = "with")
        @Schema(name = "LetterSenderSupportInfo")
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
    @Schema(name = "LetterAttachment")
    public record Attachment(

        @NotNull
        @Schema(description = "Delivery mode")
        DeliveryMode deliveryMode,

        @NotBlank
        @Schema(description = "Filename")
        String filename,

        @OneOf("application/pdf")
        @Schema(description = "Content type", allowableValues = {"application/pdf"})
        String contentType,

        @NotBlank
        @Schema(description = "Content (BASE64-encoded)")
        String content) {

        @Schema(description = """
        Delivery mode, to indicate whether an attachment is intended/allowed to be used for
        digital mail, snail-mail or any of them
        """
        )
        public enum DeliveryMode {
            ANY,
            DIGITAL_MAIL,
            SNAIL_MAIL
        }

        @JsonIgnore
        public boolean isIntendedForDigitalMail() {
            return deliveryMode == DeliveryMode.ANY || deliveryMode == DeliveryMode.DIGITAL_MAIL;
        }

        @JsonIgnore
        public boolean isIntendedForSnailMail() {
            return deliveryMode == DeliveryMode.ANY || deliveryMode == DeliveryMode.SNAIL_MAIL;
        }
    }
}