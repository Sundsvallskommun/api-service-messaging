package se.sundsvall.messaging.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnailmailRequest extends Request {
    @NotBlank
    @ValidUuid
    @Schema(description = "PersonId for the person to send snailmail to")
    private String personId;

    @ArraySchema(schema = @Schema(implementation = Attachment.class))
    private List<@Valid Attachment> attachments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    @Schema(name = "SnailmailAttachment", description = "Attachment")
    public static class Attachment {

        @NotBlank
        @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK", required = true)
        private String content;

        @NotBlank
        @Schema(description = "The attachment filename", example = "test.txt", required = true)
        private String name;

        @Schema(description = "The attachment content type", example = "text/plain")
        private String contentType;
    }
}
