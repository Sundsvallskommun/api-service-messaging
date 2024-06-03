package se.sundsvall.messaging.api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Header {
	IN_REPLY_TO,
	REFERENCES,
	MESSAGE_ID
}
