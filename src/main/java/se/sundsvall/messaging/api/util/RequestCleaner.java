package se.sundsvall.messaging.api.util;

import java.util.Optional;

public final class RequestCleaner {

	private RequestCleaner() {
		// Intentionally left empty
	}

	public static String cleanSenderName(final String sender) {
		return Optional.ofNullable(sender).map(oldString -> oldString.replaceAll("[åä]", "a")
			.replaceAll("[ö]", "o")
			.replaceAll("[ÅÄ]", "A")
			.replaceAll("[Ö]", "O"))
			.orElse(null);
	}
}
