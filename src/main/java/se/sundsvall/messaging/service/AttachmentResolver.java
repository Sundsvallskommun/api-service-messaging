package se.sundsvall.messaging.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.ReferenceableAttachment;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.integration.objectstore.ObjectStoreIntegration;
import se.sundsvall.messaging.integration.objectstore.StoredObject;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

/**
 * Replaces attachment references with the bytes they name, immediately before the request is handed to the sender.
 * <p>
 * Resolution happens here rather than in the senders so that the reference never becomes part of a public contract
 * that a service talking to Exchange would have to honour, and it happens on the way out rather than on the way in so
 * that what is stored and archived is the reference - which is the whole point of the exercise, since the alternative
 * puts megabytes of base64 back into the message row.
 * <p>
 * The bulkhead is not decoration. Each attachment in flight costs several times its own size in transient heap: the
 * response body, the base64 string built from it, Jackson's serialization of the outbound request, and logbook's
 * masking, which has to parse that body in full. Bounding one request's attachments says nothing about how many
 * requests are in flight at once, and the caller no longer pays for the upload, so without a concurrency bound this
 * makes memory pressure worse than the base64 it replaces rather than better. All three channels share the one
 * bulkhead deliberately: the bound exists to cap heap across every attachment being resolved at once, and a bulkhead
 * per channel would multiply that ceiling by the number of channels.
 */
@Component
public class AttachmentResolver {

	static final String BULKHEAD_NAME = "attachmentResolver";

	private static final Logger LOG = LoggerFactory.getLogger(AttachmentResolver.class);

	private final ObjectStoreIntegration objectStore;
	private final DataSize maxTotalSize;

	AttachmentResolver(
		final ObjectStoreIntegration objectStore,
		@Value("${messaging.attachment.max-total-size:15MB}") final DataSize maxTotalSize) {
		this.objectStore = objectStore;
		this.maxTotalSize = maxTotalSize;
	}

	@Bulkhead(name = BULKHEAD_NAME)
	public EmailRequest resolve(final EmailRequest request) {
		return resolveAll(request.attachments(), this::resolveOne)
			.map(request::withAttachments)
			.orElse(request);
	}

	@Bulkhead(name = BULKHEAD_NAME)
	public DigitalMailRequest resolve(final DigitalMailRequest request) {
		return resolveAll(request.attachments(), this::resolveOne)
			.map(request::withAttachments)
			.orElse(request);
	}

	@Bulkhead(name = BULKHEAD_NAME)
	public SnailMailRequest resolve(final SnailMailRequest request) {
		return resolveAll(request.attachments(), this::resolveOne)
			.map(request::withAttachments)
			.orElse(request);
	}

	/**
	 * @return the resolved attachments, or empty when none of them carried a reference - the caller then keeps the
	 *         request it already had rather than rebuilding an identical one
	 */
	private <A extends ReferenceableAttachment> Optional<List<A>> resolveAll(final List<A> attachments, final UnaryOperator<A> resolveOne) {
		final var present = ofNullable(attachments).orElse(List.of());

		if (present.stream().noneMatch(attachment -> isNotBlank(attachment.objectId()))) {
			// Nothing to fetch. Checked before the size guard so that a request that was legal yesterday stays legal,
			// whatever the cap is set to - the guard is about what this service now fetches on a caller's behalf.
			return Optional.empty();
		}

		final var resolved = present.stream().map(resolveOne).toList();

		guardTotalSize(resolved);

		return Optional.of(resolved);
	}

	private EmailRequest.Attachment resolveOne(final EmailRequest.Attachment attachment) {
		if (!isNotBlank(attachment.objectId())) {
			return attachment;
		}

		final var object = fetch(attachment.objectId());

		return attachment
			.withContent(encode(object))
			.withContentType(StringUtils.firstNonBlank(attachment.contentType(), object.contentType(), APPLICATION_OCTET_STREAM_VALUE))
			.withObjectId(null);
	}

	private SnailMailRequest.Attachment resolveOne(final SnailMailRequest.Attachment attachment) {
		if (!isNotBlank(attachment.objectId())) {
			return attachment;
		}

		final var object = fetch(attachment.objectId());

		return attachment
			.withContent(encode(object))
			.withContentType(StringUtils.firstNonBlank(attachment.contentType(), object.contentType(), APPLICATION_OCTET_STREAM_VALUE))
			.withObjectId(null);
	}

	private DigitalMailRequest.Attachment resolveOne(final DigitalMailRequest.Attachment attachment) {
		if (!isNotBlank(attachment.objectId())) {
			return attachment;
		}

		final var object = fetch(attachment.objectId());

		return attachment
			.withContent(encode(object))
			// Deliberately not falling back to what the store holds, the way the other two channels do. Digital mail
			// constrains the content type to application/pdf, so a stored text/plain would be carried past a boundary
			// that has already validated and rejected only later, at the sender.
			.withContentType(StringUtils.firstNonBlank(attachment.contentType(), APPLICATION_PDF_VALUE))
			.withObjectId(null);
	}

	private static String encode(final StoredObject object) {
		return Base64.getEncoder().encodeToString(object.content());
	}

	private StoredObject fetch(final String objectId) {
		try {
			return objectStore.fetch(objectId);
		} catch (final ClientProblem e) {
			// The client bypasses 404, so this is the object being unknown or expired rather than the store being
			// unwell. That is the caller's reference being wrong, and saying so beats passing a 404 up from an endpoint
			// that does not document one - clients read that as "no such endpoint".
			LOG.info("Attachment object {} could not be read: {}", objectId, e.getMessage());
			throw Problem.valueOf(BAD_REQUEST, "Attachment object " + objectId + " does not exist or is no longer available");
		}
	}

	private void guardTotalSize(final List<? extends ReferenceableAttachment> attachments) {
		final var totalBytes = attachments.stream()
			.map(ReferenceableAttachment::content)
			.filter(StringUtils::isNotBlank)
			.mapToLong(AttachmentResolver::decodedLength)
			.sum();

		if (totalBytes > maxTotalSize.toBytes()) {
			throw Problem.valueOf(PAYLOAD_TOO_LARGE,
				"Attachments total %d bytes, which is more than the %d allowed".formatted(totalBytes, maxTotalSize.toBytes()));
		}
	}

	/**
	 * The decoded size of a base64 string, worked out from its length rather than by decoding it - the point of the
	 * guard is to avoid holding the bytes, so decoding them to find out how many there are would defeat it.
	 */
	private static long decodedLength(final String base64) {
		final var padding = base64.chars().filter(character -> character == '=').count();
		return (base64.length() / 4L) * 3L - padding;
	}
}
