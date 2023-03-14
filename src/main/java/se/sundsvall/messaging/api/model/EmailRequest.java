package se.sundsvall.messaging.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import se.sundsvall.messaging.model.PartyWithOptionalPartyId;
import se.sundsvall.messaging.model.Sender;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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
public class EmailRequest extends Request {

    @Valid
    @Schema(description = "Party")
    private PartyWithOptionalPartyId party;

    @Schema(description = "Recipient e-mail address", requiredMode = REQUIRED)
    @Email
    @NotBlank
    private String emailAddress;

    @Schema(description = "E-mail subject", requiredMode = REQUIRED)
    @NotBlank
    private String subject;

    @Schema(description = "E-mail plain-text body")
    private String message;

    @Schema(description = "E-mail HTML body (BASE64-encoded)")
    private String htmlMessage;

    @Valid
    @Schema(description = "Sender")
    private Sender.Email sender;

    @ArraySchema(schema = @Schema(implementation = Attachment.class))
    private List<@Valid Attachment> attachments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    @Schema(name = "EmailAttachment", description = "Attachment")
    public static class Attachment {

        @NotBlank
        @Schema(
            description = "The attachment (file) content as a BASE64-encoded string",
            example = "aGVsbG8gd29ybGQK",
            requiredMode = REQUIRED
        )
        private String content;

        @NotBlank
        @Schema(
            description = "The attachment filename",
            example = "test.txt",
            requiredMode = REQUIRED
        )
        private String name;

        @Schema(description = "The attachment content type", example = "text/plain")
        private String contentType;
    }
}
