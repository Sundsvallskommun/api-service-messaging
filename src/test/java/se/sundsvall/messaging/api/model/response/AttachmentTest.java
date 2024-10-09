package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class AttachmentTest {

	private static final String CONTENT_TYPE = "contentType";
	private static final String FILE_NAME = "fileName";
	private static final String CONTENT = "content";

	@Test
	void attachmentConstructor() {
		var attachment = new Attachment(CONTENT_TYPE, FILE_NAME, CONTENT);

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(attachment.fileName()).isEqualTo(FILE_NAME);
		assertThat(attachment.content()).isEqualTo(CONTENT);
	}

	@Test
	void attachmentBuilder() {
		var attachment = Attachment.builder()
			.withContentType(CONTENT_TYPE)
			.withFileName(FILE_NAME)
			.withContent(CONTENT)
			.build();

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(attachment.fileName()).isEqualTo(FILE_NAME);
		assertThat(attachment.content()).isEqualTo(CONTENT);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Attachment.builder().build()).hasAllNullFieldsOrProperties();
	}
}
