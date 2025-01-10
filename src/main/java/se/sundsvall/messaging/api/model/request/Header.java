package se.sundsvall.messaging.api.model.request;

import java.util.Arrays;
import java.util.regex.Pattern;

public enum Header {

	IN_REPLY_TO("In-Reply-To", "^<.{1,1000}@.{1,1000}>$", "must start with '<', contain '@' and end with '>'"),
	REFERENCES("References", "^<.{1,1000}@.{1,1000}>$", "must start with '<', contain '@' and end with '>'"),
	MESSAGE_ID("Message-ID", "^<.{1,1000}@.{1,1000}>$", "must start with '<', contain '@' and end with '>'"),
	AUTO_SUBMITTED("Auto-Submitted", "^(auto-generated)$", "must be equal to 'auto-generated'");

	private final String key;
	private final Pattern pattern;
	private final String message;

	Header(final String key, final String pattern, final String message) {
		this.key = key;
		this.pattern = Pattern.compile(pattern);
		this.message = message;
	}

	public static Header fromString(final String s) {
		return Arrays.stream(Header.values())
			.filter(header -> header.key.equals(s) || header.name().equals(s))
			.findFirst()
			.orElse(null);
	}

	public String getKey() {
		return key;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public String getMessage() {
		return message;
	}
}
