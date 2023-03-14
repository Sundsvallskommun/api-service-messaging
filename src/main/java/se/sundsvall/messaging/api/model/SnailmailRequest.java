package se.sundsvall.messaging.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import se.sundsvall.messaging.model.PartyWithOptionalPartyId;

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
public class SnailmailRequest extends Request {

    @Valid
    @Schema(description = "Party")
    private PartyWithOptionalPartyId party;

    @NotBlank
    @Schema(description = "Department and unit that should be billed", example = "SBK(Gatuavdelningen, Trafiksektionen)")
    private String department;

    @Schema(description = "If the letter to send deviates from the standard", example = "A3 Ritning")
    private String deviation;

    @NotEmpty
    @ArraySchema(schema = @Schema(implementation = Attachment.class), minItems = 1)
    private List<@Valid Attachment> attachments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    @Schema(name = "SnailmailAttachment", description = "Attachment")
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
