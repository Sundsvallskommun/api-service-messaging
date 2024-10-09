package se.sundsvall.messaging.api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(description = "Attachment model")
public record Attachment(

	@Schema(description = "The attachment content type", example = "application/pdf")
	String contentType,

	@Schema(description = "The attachment file name", example = "attachment.pdf")
	String fileName,

	@Schema(description = "The attachment content")
	String content
) {
}
