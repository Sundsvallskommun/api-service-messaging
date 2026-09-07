package se.sundsvall.messaging.integration.objectstore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Component
@EnableConfigurationProperties(ObjectStoreIntegrationProperties.class)
public class ObjectStoreIntegration {

	static final String INTEGRATION_NAME = "ObjectStore";

	/**
	 * Matches the plain {@code filename="…"} parameter. The store emits RFC 6266 with both {@code filename} and
	 * {@code filename*}; reading the plain one is enough for a fallback, and a name that does not survive it simply
	 * falls through to whatever the attachment itself was given.
	 */
	private static final Pattern FILE_NAME_PATTERN = Pattern.compile("filename=\"([^\"]*)\"");

	private final ObjectStoreClient client;
	private final ObjectStoreIntegrationProperties properties;

	ObjectStoreIntegration(final ObjectStoreClient client, final ObjectStoreIntegrationProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	/**
	 * Reads one object from the configured bucket.
	 * <p>
	 * Propagates the integration's problem on failure, and the distinction matters to the caller: an unknown or expired
	 * object arrives as a 404 because the client bypasses that status, while anything else has been rewritten to
	 * BAD_GATEWAY. The first is the reference being wrong, the second is the store being unwell, and only the first is
	 * worth telling the sender about.
	 */
	public StoredObject fetch(final String objectId) {
		final var response = client.readObject(properties.getBucket(), objectId);

		return new StoredObject(
			response.getBody(),
			ofNullable(response.getHeaders().getContentType()).map(Object::toString).orElse(null),
			fileNameFrom(response.getHeaders().getFirst(CONTENT_DISPOSITION)));
	}

	private static String fileNameFrom(final String contentDisposition) {
		return ofNullable(contentDisposition)
			.map(FILE_NAME_PATTERN::matcher)
			.filter(Matcher::find)
			.map(matcher -> matcher.group(1))
			.filter(fileName -> !fileName.isBlank())
			.orElse(null);
	}
}
