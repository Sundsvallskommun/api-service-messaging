package se.sundsvall.messaging.integration.oepintegrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiremock.org.apache.commons.io.FileUtils;

@ExtendWith(MockitoExtension.class)
class AttachmentMultipartFileTest {

	@Mock
	private InputStream inputStreamMock;

	@Test
	void create() throws IOException {
		var fileName = "test.txt";
		var mimeType = "text/plain";
		when(inputStreamMock.available()).thenReturn(1);
		when(inputStreamMock.readAllBytes()).thenReturn(new byte[0]);

		var result = AttachmentMultipartFile.create(fileName, mimeType, inputStreamMock);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(fileName);
		assertThat(result.getOriginalFilename()).isEqualTo(fileName);
		assertThat(result.getContentType()).isEqualTo(mimeType);
		assertThat(result.isEmpty()).isFalse();
		assertThat(result.getSize()).isEqualTo(1);
		assertThat(result.getBytes()).isEqualTo(new byte[0]);
		assertThat(result.getInputStream()).isEqualTo(inputStreamMock);
	}

	@Test
	void transferToForAttachmentWithContent() throws Exception {
		final var fileName = "test.txt";
		final var mimeType = "text/plain";
		final var content = "Test content".getBytes();
		final var contentStream = new ByteArrayInputStream(content);
		final var multipartFile = AttachmentMultipartFile.create(fileName, mimeType, contentStream);
		final var file = File.createTempFile("test_", null);

		multipartFile.transferTo(file);

		assertThat(file).exists();
		assertThat(FileUtils.readFileToByteArray(file)).isEqualTo(content);
	}

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void createWithNullArguments(final String fileName, final String mimeType, final InputStream inputStream, final String errorMessage) {
		assertThatThrownBy(() -> AttachmentMultipartFile.create(fileName, mimeType, inputStream))
			.isInstanceOf(NullPointerException.class)
			.hasMessage(errorMessage);
	}

	static Stream<Arguments> argumentProvider() {
		return Stream.of(
			Arguments.of(null, "text/plain", new ByteArrayInputStream("Test".getBytes()), "Filename must be provided"),
			Arguments.of("test.txt", null, new ByteArrayInputStream("Test".getBytes()), "MimeType must be provided"),
			Arguments.of("test.txt", "text/plain", null, "Content stream must be provided"));
	}

}
