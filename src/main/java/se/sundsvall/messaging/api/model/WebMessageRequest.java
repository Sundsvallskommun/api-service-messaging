package se.sundsvall.messaging.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.messaging.model.PartyWithRequiredPartyId;

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
public class WebMessageRequest extends Request {

    @Valid
    @NotNull
    @Schema(description = "Party", requiredMode = REQUIRED)
    private PartyWithRequiredPartyId party;

    @NotBlank
    @Schema(description = "Message", requiredMode = REQUIRED)
    private String message;

    @ArraySchema(schema = @Schema(implementation = Attachment.class), maxItems = 10)
    private List<@Valid Attachment> attachments;

    @Getter
    @Setter
    @Builder(setterPrefix = "with")
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(name = "WebMessageAttachment", description = "Attachment")
    public static class Attachment {

        @NotBlank
        @Schema(description = "File name")
        private String fileName;

        @Schema(description = "Mime-type")
        private String mimeType;

        @ValidBase64
        @Schema(description = "BASE64-encoded file, max size 10 MB")
        private String base64Data;
    }
}