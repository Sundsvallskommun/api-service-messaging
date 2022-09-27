package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.constraints.NotBlank;

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

    @NotBlank
    @Schema(description = "Message", required = true)
    private String message;

    @ArraySchema(schema = @Schema(implementation = Attachment.class), maxItems = 10)
    private List<Attachment> attachments;

    @Getter
    @Setter
    @Builder(setterPrefix = "with")
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(name = "WebMessageAttachment", description = "Attachment")
    public static class Attachment {

        @Schema(description = "File name")
        private String fileName;

        @Schema(description = "Mime-type")
        private String mimeType;

        @Schema(description = "BASE64-encoded file, max size 10 MB")
        private String base64Data;
    }
}