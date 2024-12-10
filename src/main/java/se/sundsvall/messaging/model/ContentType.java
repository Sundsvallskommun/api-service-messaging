package se.sundsvall.messaging.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import org.springframework.http.MediaType;

public enum ContentType {

	TEXT_PLAIN(MediaType.TEXT_PLAIN_VALUE),
	TEXT_HTML(MediaType.TEXT_HTML_VALUE),
	APPLICATION_PDF(MediaType.APPLICATION_PDF_VALUE);

	private final String value;

	ContentType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@JsonCreator
	public static ContentType fromString(final String s) {
		return Arrays.stream(ContentType.values())
			.filter(contentType -> contentType.value.equals(s))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unhandled content type: '" + s + "'"));
	}
}
