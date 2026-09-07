package se.sundsvall.messaging.service;

import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.integration.objectstore.ObjectStoreIntegration;
import se.sundsvall.messaging.integration.objectstore.StoredObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

@ExtendWith(MockitoExtension.class)
class AttachmentResolverTest {

	private static final String OBJECT_ID = "f8e2bd3c-1a6b-4f5e-9d0a-2c7b1e4f6a58";
	private static final byte[] BYTES = "hello world".getBytes();
	private static final String BYTES_AS_BASE64 = Base64.getEncoder().encodeToString(BYTES);

	@Mock
	private ObjectStoreIntegration mockObjectStore;

	private AttachmentResolver resolver() {
		return resolver(DataSize.ofMegabytes(15));
	}

	private AttachmentResolver resolver(final DataSize maxTotalSize) {
		return new AttachmentResolver(mockObjectStore, maxTotalSize);
	}

	private static EmailRequest requestWith(final EmailRequest.Attachment... attachments) {
		return EmailRequest.builder()
			.withSubject("subject")
			.withAttachments(List.of(attachments))
			.build();
	}

	private static EmailRequest.Attachment reference() {
		return EmailRequest.Attachment.builder().withName("file.txt").withObjectId(OBJECT_ID).build();
	}

	@Test
	void resolve_requestWithoutAttachmentsIsUntouched() {
		final var request = EmailRequest.builder().withSubject("subject").build();

		assertThat(resolver().resolve(request)).isSameAs(request);
		verifyNoInteractions(mockObjectStore);
	}

	@Test
	void resolve_inlineAttachmentsAreLeftAloneAndNothingIsFetched() {
		// The store is never called for a request that names no objects, and the request comes back as the same
		// instance - a base64 caller must not be able to tell that any of this exists.
		final var request = requestWith(EmailRequest.Attachment.builder()
			.withName("file.txt")
			.withContent(BYTES_AS_BASE64)
			.build());

		assertThat(resolver().resolve(request)).isSameAs(request);
		verifyNoInteractions(mockObjectStore);
	}

	@Test
	void resolve_referenceIsReplacedByItsContent() {
		when(mockObjectStore.fetch(OBJECT_ID)).thenReturn(new StoredObject(BYTES, "text/plain", "stored.txt"));

		final var attachment = resolver().resolve(requestWith(reference())).attachments().getFirst();

		assertThat(attachment.content()).isEqualTo(BYTES_AS_BASE64);
		// The reference is cleared once resolved: what goes to the sender is an ordinary attachment, and leaving both
		// set would produce the very shape the request validator rejects.
		assertThat(attachment.objectId()).isNull();
		assertThat(attachment.name()).isEqualTo("file.txt");
	}

	@Test
	void resolve_contentTypeFallsBackToTheStoredOne() {
		when(mockObjectStore.fetch(OBJECT_ID)).thenReturn(new StoredObject(BYTES, "application/pdf", "stored.txt"));

		assertThat(resolver().resolve(requestWith(reference())).attachments().getFirst().contentType())
			.isEqualTo("application/pdf");
	}

	@Test
	void resolve_callerContentTypeWinsOverTheStoredOne() {
		when(mockObjectStore.fetch(OBJECT_ID)).thenReturn(new StoredObject(BYTES, "application/pdf", "stored.txt"));

		final var attachment = reference().withContentType("text/csv");

		assertThat(resolver().resolve(requestWith(attachment)).attachments().getFirst().contentType())
			.isEqualTo("text/csv");
	}

	@Test
	void resolve_contentTypeFallsBackToOctetStreamWhenNobodyKnows() {
		// email-sender requires a non-blank content type while this service treats it as optional, so a blank one has
		// to become something before it leaves rather than failing at delivery.
		when(mockObjectStore.fetch(OBJECT_ID)).thenReturn(new StoredObject(BYTES, null, null));

		assertThat(resolver().resolve(requestWith(reference())).attachments().getFirst().contentType())
			.isEqualTo("application/octet-stream");
	}

	@Test
	void resolve_unknownOrExpiredObjectIsTheCallersFaultNotTheStores() {
		// The client bypasses 404, so this arrives as a ClientProblem. It has to become a 400 naming the object: an
		// unmapped 404 out of a send endpoint reads to a client as "no such endpoint".
		when(mockObjectStore.fetch(anyString())).thenThrow(new ClientProblem(BAD_GATEWAY, "not found"));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> resolver().resolve(requestWith(reference())))
			.satisfies(problem -> {
				assertThat(problem.getStatusCode()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getMessage()).contains(OBJECT_ID);
			});
	}

	@Test
	void resolve_totalSizeIsCapped() {
		when(mockObjectStore.fetch(OBJECT_ID)).thenReturn(new StoredObject(new byte[2048], "text/plain", null));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> resolver(DataSize.ofBytes(1024)).resolve(requestWith(reference())))
			.satisfies(problem -> assertThat(problem.getStatusCode()).isEqualTo(PAYLOAD_TOO_LARGE));
	}

	@Test
	void resolve_capCountsInlineAttachmentsAlongsideResolvedOnes() {
		// The guard is about what the whole message weighs, not only about what this service fetched on the caller's
		// behalf - otherwise a reference plus a large inline attachment slips past a cap that either alone would hit.
		when(mockObjectStore.fetch(OBJECT_ID)).thenReturn(new StoredObject(new byte[600], "text/plain", null));

		final var inline = EmailRequest.Attachment.builder()
			.withName("big.bin")
			.withContent(Base64.getEncoder().encodeToString(new byte[600]))
			.build();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> resolver(DataSize.ofBytes(1024)).resolve(requestWith(reference(), inline)))
			.satisfies(problem -> assertThat(problem.getStatusCode()).isEqualTo(PAYLOAD_TOO_LARGE));
	}
}
