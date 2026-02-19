package se.sundsvall.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.party.PartyIntegration;
import se.sundsvall.messaging.model.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_INVOICE;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

/**
 * Testing the extraction of attachment handling in the HistoryService.
 *
 */

@ExtendWith(MockitoExtension.class)
class HistoryServiceAttachmentTest {

	@Mock
	private DbIntegration mockDbIntegration;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private BatchExtractor batchExtractorMock;

	private HistoryService historyService;

	private static final String EMPTY_ATTACHMENT = """
		{
		  "attachments": []
		}
		""";

	private static final String NO_ATTACHMENT = """
		{
		}
		""";

	@BeforeEach
	void setUp() {
		final var objectMapper = new ObjectMapper();
		historyService = new HistoryService(mockDbIntegration, partyIntegrationMock, objectMapper, batchExtractorMock);
	}

	@AfterEach
	void tearDown() {
		verifyNoInteractions(mockDbIntegration, partyIntegrationMock);
	}

	@Test
	void testGetAttachmentForEmail() {
		final var historyEntity = HistoryEntity.builder()
			.withMessageType(EMAIL)
			.withContent("""
				{
				  "attachments": [
				    {
				      "name": "test.pdf",
				      "contentType": "application/pdf",
				      "content": "aGVsbG8gd29ybGQK"
				    }
				  ]
				}
				""")
			.build();

		final var messageAttachments = historyService.extractAttachment(historyEntity);
		assertThat(messageAttachments).isNotNull().hasSize(1);
		assertThat(messageAttachments.getFirst().fileName()).isEqualTo("test.pdf");
		assertThat(messageAttachments.getFirst().contentType()).isEqualTo("application/pdf");
	}

	@Test
	void testGetAttachmentForWebMessage() {
		final var historyEntity = HistoryEntity.builder()
			.withMessageType(WEB_MESSAGE)
			.withContent("""
				{
				  "attachments": [
				    {
				      "fileName": "test.pdf",
				      "mimeType": "application/pdf",
				      "base64Data": "aGVsbG8gd29ybGQK"
				    }
				  ]
				}
				""")
			.build();

		final var messageAttachments = historyService.extractAttachment(historyEntity);
		assertThat(messageAttachments).isNotNull().hasSize(1);
		assertThat(messageAttachments.getFirst().fileName()).isEqualTo("test.pdf");
		assertThat(messageAttachments.getFirst().contentType()).isEqualTo("application/pdf");
	}

	@Test
	void testGetAttachmentForDigitalMail() {
		final var historyEntity = HistoryEntity.builder()
			.withMessageType(DIGITAL_MAIL)
			.withContent("""
				{
				  "attachments": [
				    {
				      "filename": "test.pdf",
				      "contentType": "application/pdf",
				      "content": "aGVsbG8gd29ybGQK"
				    }
				  ]
				}
				""")
			.build();

		final var messageAttachments = historyService.extractAttachment(historyEntity);
		assertThat(messageAttachments).isNotNull().hasSize(1);
		assertThat(messageAttachments.getFirst().fileName()).isEqualTo("test.pdf");
		assertThat(messageAttachments.getFirst().contentType()).isEqualTo("application/pdf");
	}

	@Test
	void testGetAttachmentForDigitalInvoice() {
		final var historyEntity = HistoryEntity.builder()
			.withMessageType(DIGITAL_INVOICE)
			.withContent("""
				{
				  "files": [
				       {
				         "filename": "test.pdf",
				         "contentType": "application/pdf",
				         "content": "aGVsbG8gd29ybGQK"
				       }
				     ]
				}
				""")
			.build();

		final var messageAttachments = historyService.extractAttachment(historyEntity);
		assertThat(messageAttachments).isNotNull().hasSize(1);
		assertThat(messageAttachments.getFirst().fileName()).isEqualTo("test.pdf");
		assertThat(messageAttachments.getFirst().contentType()).isEqualTo("application/pdf");
	}

	@Test
	void testGetAttachmentForLetter() {
		final var historyEntity = HistoryEntity.builder()
			.withMessageType(LETTER)
			.withContent("""
				{
				  "attachments": [
				       {
				         "filename": "test.pdf",
				         "contentType": "application/pdf",
				         "content": "aGVsbG8gd29ybGQK",
				         "deliveryMode": "ANY"
				       }
				     ]
				}
				""")
			.build();

		final var messageAttachments = historyService.extractAttachment(historyEntity);
		assertThat(messageAttachments).isNotNull().hasSize(1);
		assertThat(messageAttachments.getFirst().fileName()).isEqualTo("test.pdf");
		assertThat(messageAttachments.getFirst().contentType()).isEqualTo("application/pdf");
	}

	@ParameterizedTest
	@MethodSource("attachmentSource")
	void testGetMissingOrEmptyAttachments_shouldReturnEmptyAttachments(MessageType messageType, String attachment) {
		final var historyEntity = HistoryEntity.builder()
			.withMessageType(messageType)
			.withContent(attachment)
			.build();

		final var messageAttachments = historyService.extractAttachment(historyEntity);

		assertThat(messageAttachments).isEmpty();
	}

	private static Stream<Arguments> attachmentSource() {
		return Stream.of(
			Arguments.of(EMAIL, EMPTY_ATTACHMENT),
			Arguments.of(EMAIL, NO_ATTACHMENT),
			Arguments.of(EMAIL, null),
			Arguments.of(WEB_MESSAGE, EMPTY_ATTACHMENT),
			Arguments.of(WEB_MESSAGE, NO_ATTACHMENT),
			Arguments.of(WEB_MESSAGE, null),
			Arguments.of(DIGITAL_MAIL, EMPTY_ATTACHMENT),
			Arguments.of(DIGITAL_MAIL, NO_ATTACHMENT),
			Arguments.of(DIGITAL_MAIL, null),
			Arguments.of(DIGITAL_INVOICE, EMPTY_ATTACHMENT),
			Arguments.of(DIGITAL_INVOICE, NO_ATTACHMENT),
			Arguments.of(DIGITAL_INVOICE, null),
			// These cannot have attachments
			Arguments.of(MESSAGE, null),
			Arguments.of(SMS, null),
			Arguments.of(SLACK, null));
	}

}
