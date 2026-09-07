package se.sundsvall.messaging.integration.objectstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@ExtendWith(MockitoExtension.class)
class ObjectStoreIntegrationTest {

	private static final String BUCKET = "messaging-attachments";
	private static final String OBJECT_ID = "f8e2bd3c-1a6b-4f5e-9d0a-2c7b1e4f6a58";
	private static final byte[] CONTENT = "hello world".getBytes();

	@Mock
	private ObjectStoreClient mockClient;

	private ObjectStoreIntegration integration() {
		final var properties = new ObjectStoreIntegrationProperties();
		properties.setBucket(BUCKET);
		return new ObjectStoreIntegration(mockClient, properties);
	}

	private void stubResponse(final HttpHeaders headers) {
		when(mockClient.readObject(BUCKET, OBJECT_ID)).thenReturn(new ResponseEntity<>(CONTENT, headers, 200));
	}

	private static HttpHeaders headers(final String contentType, final String contentDisposition) {
		final var headers = new HttpHeaders();
		if (contentType != null) {
			headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		}
		if (contentDisposition != null) {
			headers.add(CONTENT_DISPOSITION, contentDisposition);
		}
		return headers;
	}

	@Test
	void fetch_readsFromTheConfiguredBucket() {
		stubResponse(headers(APPLICATION_PDF_VALUE, "attachment; filename=\"stored.pdf\""));

		final var result = integration().fetch(OBJECT_ID);

		// The bucket is never the caller's to choose - it comes from configuration on every read.
		verify(mockClient).readObject(BUCKET, OBJECT_ID);
		assertThat(result.content()).isEqualTo(CONTENT);
		assertThat(result.contentType()).isEqualTo(APPLICATION_PDF_VALUE);
		assertThat(result.fileName()).isEqualTo("stored.pdf");
	}

	@Test
	void fetch_toleratesAnObjectStoredWithoutHeaders() {
		// Both are fallbacks for what the attachment itself did not say, so their absence is ordinary rather than an
		// error - the resolver falls through to application/octet-stream from here.
		stubResponse(headers(null, null));

		final var result = integration().fetch(OBJECT_ID);

		assertThat(result.contentType()).isNull();
		assertThat(result.fileName()).isNull();
	}

	@Test
	void fetch_ignoresAContentDispositionWithoutAPlainFilename() {
		// Only the plain filename parameter is read; a header carrying just the extended form falls back to null
		// rather than handing on a percent-encoded value.
		stubResponse(headers(APPLICATION_PDF_VALUE, "attachment; filename*=UTF-8''r%C3%A4kning.pdf"));

		assertThat(integration().fetch(OBJECT_ID).fileName()).isNull();
	}

	@Test
	void fetch_ignoresABlankFilename() {
		stubResponse(headers(APPLICATION_PDF_VALUE, "attachment; filename=\"\""));

		assertThat(integration().fetch(OBJECT_ID).fileName()).isNull();
	}
}
