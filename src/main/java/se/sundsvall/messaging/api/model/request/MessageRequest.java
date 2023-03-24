package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record MessageRequest(

    @NotEmpty
    @Schema(description = "The messages to be sent", requiredMode = REQUIRED)
    List<@Valid Message> messages) {

    @With
    @Builder(setterPrefix = "with")
    public record Message(

        @Valid
        @NotNull
        @Schema(description = "Party", requiredMode = REQUIRED)
        Party party,

        @Schema(description = "Headers")
        List<@Valid Header> headers,

        @Valid
        @Schema(description = "Sender")
        Sender sender,

        @Schema(description = "The message subject (for E-mails)")
        String subject,

        @NotBlank
        @Schema(description = "Plain-text message text", requiredMode = REQUIRED)
        String message,

        @Schema(description = "HTML message text, for e-mails (BASE64-encoded)")
        String htmlMessage) {

        @With
        @Builder(setterPrefix = "with")
        @Schema(name = "MessageParty")
        public record Party(

            @ValidUuid
            @Schema(description = "The message party id", format = "uuid", requiredMode = REQUIRED)
            String partyId,

            @Schema(description = "External references")
            List<@Valid ExternalReference> externalReferences) {
        }

        @With
        @Builder(setterPrefix = "with")
        @Schema(name = "MessageSender")
        public record Sender(

            @Valid
            Email email,

            @Valid
            Sms sms) {

            @With
            @Builder(setterPrefix = "with")
            public record Email(

                @NotBlank
                @Schema(description = "The sender of the e-mail")
                String name,

                @javax.validation.constraints.Email
                @NotBlank
                @Schema(description = "Sender e-mail address", example = "sender@sender.se")
                String address,

                @Schema(description = "Reply-to e-mail address", example = "sender@sender.se")
                @javax.validation.constraints.Email
                String replyTo) { }

            @With
            @Builder(setterPrefix = "with")
            public record Sms(

                @NotBlank
                @Size(max = 11)
                @Schema(description = "The sender of the SMS", maxLength = 11, example = "sender")
                String name) { }
        }
    }
}