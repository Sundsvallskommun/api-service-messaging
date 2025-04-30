package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.messaging.TestDataFactory.createAttachment;
import static se.sundsvall.messaging.TestDataFactory.createHistoryEntity;
import static se.sundsvall.messaging.TestDataFactory.createUserMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import se.sundsvall.messaging.api.model.response.UserMessage;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.MessageIdProjection;
import se.sundsvall.messaging.integration.party.PartyIntegration;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

	@Mock
	private DbIntegration mockDbIntegration;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private HttpServletResponse httpServletResponseMock;

	@Mock
	private ObjectMapper objectMapper;

	@Captor
	private ArgumentCaptor<PageRequest> pageRequestCaptor;

	@InjectMocks
	private HistoryService historyService;

	@Test
	void test_getHistoryByMunicipalityIdAndMessageId() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndMessageId("2281", "someMessageId");

		assertThat(result).isNotEmpty();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndMessageId_whenNoEntityExists() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of());

		final var result = historyService.getHistoryByMunicipalityIdAndMessageId("2281", "someMessageId");

		assertThat(result).isEmpty();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByBatchId() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndBatchId(any(String.class), any(String.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndBatchId("2281", "someBatchId");

		assertThat(result).hasSize(1);

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndBatchId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndDeliveryId() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndDeliveryId("2281", "someBatchId");

		assertThat(result).isPresent();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndDeliveryId_whenNoEntityExists() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		final var result = historyService.getHistoryByMunicipalityIdAndDeliveryId("2281", "someBatchId");

		assertThat(result).isEmpty();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class));
	}

	@Test
	void test_getConversationHistory() {
		when(mockDbIntegration.getHistory(any(String.class), any(String.class), nullable(LocalDate.class), nullable(LocalDate.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getConversationHistory("2281", "somePartyId", null, null);

		assertThat(result).hasSize(1);

		verify(mockDbIntegration, times(1))
			.getHistory(any(String.class), any(String.class), nullable(LocalDate.class), nullable(LocalDate.class));
	}

	@Test
	void streamAttachmentTest() throws IOException {
		var municipalityId = "2281";
		var messageId = "someMessageId";
		var historyEntity = createHistoryEntity();
		var attachment = createAttachment();
		var spy = Mockito.spy(historyService);
		when(httpServletResponseMock.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		when(mockDbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(historyEntity);
		doReturn(attachment).when(spy).findAttachmentByName(any(), any(), any());

		try (final MockedStatic<StreamUtils> streamMock = Mockito.mockStatic(StreamUtils.class)) {
			spy.streamAttachment(municipalityId, messageId, "someFileName", httpServletResponseMock);

			verify(httpServletResponseMock).addHeader(CONTENT_TYPE, attachment.getContentType());
			verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"");
			streamMock.verify(() -> StreamUtils.copy(any(InputStream.class), any(OutputStream.class)));
		}
	}

	@Test
	void getUserMessagesTest() {
		var municipalityId = "2281";
		var userId = "someUserId";
		var limit = 10;
		var page = 1;
		var userMessageList = List.of(createUserMessage(), createUserMessage());
		var spy = Mockito.spy(historyService);
		var messageIdProjectionMock = Mockito.mock(MessageIdProjection.class);
		Page<MessageIdProjection> messageIdPage = new PageImpl<>(List.of(messageIdProjectionMock, messageIdProjectionMock, messageIdProjectionMock));
		when(mockDbIntegration.getUniqueMessageIds(eq(municipalityId), eq(userId), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(messageIdPage);
		doReturn(userMessageList).when(spy).createUserMessages(municipalityId, messageIdPage.getContent());

		var result = spy.getUserMessages(municipalityId, userId, page, limit);

		assertThat(result).isNotNull().satisfies(userMessages -> {
			assertThat(userMessages.messages()).isEqualTo(userMessageList);
			assertThat(userMessages.metaData().getPage()).isEqualTo(1);
			assertThat(userMessages.metaData().getLimit()).isEqualTo(3);
			assertThat(userMessages.metaData().getCount()).isEqualTo(3);
			assertThat(userMessages.metaData().getTotalRecords()).isEqualTo(3);
			assertThat(userMessages.metaData().getTotalPages()).isEqualTo(1);
		});
		verify(mockDbIntegration).getUniqueMessageIds(eq(municipalityId), eq(userId), any(LocalDateTime.class), pageRequestCaptor.capture());
		var pageRequest = pageRequestCaptor.getValue();
		assertThat(pageRequest.getPageNumber()).isZero();
		assertThat(pageRequest.getPageSize()).isEqualTo(limit);
	}

	@Test
	void createUserMessagesTest() {
		var municipalityId = "2281";
		var messageIdProjectionMock = Mockito.mock(MessageIdProjection.class);
		var spy = Mockito.spy(historyService);
		when(messageIdProjectionMock.getMessageId()).thenReturn("1");
		doReturn(createUserMessage()).when(spy).createUserMessage(eq(municipalityId), any());

		var result = spy.createUserMessages(municipalityId, List.of(messageIdProjectionMock, messageIdProjectionMock, messageIdProjectionMock));

		assertThat(result).isNotNull().hasSize(3);
	}

	@Test
	void createUserMessageTest() {
		var municipalityId = "2281";
		var messageId = "someMessageId";
		var histories = List.of(createHistoryEntity());
		var spy = Mockito.spy(historyService);
		when(mockDbIntegration.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(histories);
		var subject = "someSubject";
		doReturn(subject).when(spy).extractSubject(histories.getFirst());
		var recipients = List.of(UserMessage.Recipient.builder().withMessageType("SNAIL_MAIL").withPersonId("123456-7890"));
		doReturn(recipients).when(spy).createRecipients(municipalityId, histories);
		var attachments = List.of(UserMessage.MessageAttachment.builder().withContentType("application/pdf").withFileName("someFileName").build());
		doReturn(attachments).when(spy).extractAttachment(histories.getFirst());

		var result = spy.createUserMessage(municipalityId, messageId);

		assertThat(result).isNotNull().satisfies(userMessage -> {
			assertThat(userMessage.messageId()).isEqualTo(messageId);
			assertThat(userMessage.recipients()).isEqualTo(recipients);
			assertThat(userMessage.attachments()).isEqualTo(attachments);
			assertThat(userMessage.issuer()).isEqualTo(histories.getFirst().getIssuer());
			assertThat(userMessage.origin()).isEqualTo(histories.getFirst().getOrigin());
			assertThat(userMessage.sent()).isEqualTo(histories.getFirst().getCreatedAt());
		});

		verify(mockDbIntegration).getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		verify(spy).createRecipients(municipalityId, histories);
		verify(spy).extractAttachment(histories.getFirst());
		verify(spy).createUserMessage(municipalityId, messageId);
		verifyNoMoreInteractions(mockDbIntegration, spy);
	}

	@Test
	void findAttachmentByNameTest() {
		var contentInputNode = mock(JsonNode.class);
		var attachmentArrayNode = mock(JsonNode.class);
		var attachmentNode = mock(JsonNode.class);

		var fileNameNode = mock(JsonNode.class);
		var contentTypeNode = mock(JsonNode.class);
		var contentNode = mock(JsonNode.class);

		when(contentInputNode.get("attachments")).thenReturn(attachmentArrayNode);
		when(attachmentArrayNode.isArray()).thenReturn(true);
		when(attachmentArrayNode.iterator()).thenReturn(List.of(attachmentNode).iterator());
		when(attachmentNode.get("name")).thenReturn(fileNameNode);
		when(attachmentNode.get("content")).thenReturn(contentNode);
		when(attachmentNode.get("contentType")).thenReturn(contentTypeNode);
		when(fileNameNode.asText()).thenReturn("someFileName");
		when(contentTypeNode.asText()).thenReturn("application/pdf");
		when(contentNode.asText()).thenReturn("someContent");

		var result = historyService.findAttachmentByName(contentInputNode, "name", "someFileName");

		assertThat(result).isNotNull().satisfies(attachment -> {
			assertThat(attachment.getName()).isEqualTo("someFileName");
			assertThat(attachment.getContentType()).isEqualTo("application/pdf");
			assertThat(attachment.getContent()).isEqualTo("someContent");
		});
	}

	@Test
	void findAttachmentByNameTest_2() {
		var contentInputNode = mock(JsonNode.class);
		var attachmentArrayNode = mock(JsonNode.class);

		when(contentInputNode.get("attachments")).thenReturn(attachmentArrayNode);
		when(attachmentArrayNode.isArray()).thenReturn(false);

		assertThatThrownBy(() -> historyService.findAttachmentByName(contentInputNode, "name", "someFileName"))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Attachment with name someFileName not found");
	}

	@Test
	void extractAttachmentTest() throws JsonProcessingException {
		var history = createHistoryEntity();

		var jsonNodeMock = mock(JsonNode.class);
		var attachmentArrayNode = mock(JsonNode.class);
		var attachmentNode = mock(JsonNode.class);
		var fileNameNode = mock(JsonNode.class);
		var contentTypeNode = mock(JsonNode.class);

		when(objectMapper.readTree(history.getContent())).thenReturn(jsonNodeMock);
		when(jsonNodeMock.get("attachments")).thenReturn(attachmentArrayNode);
		when(attachmentArrayNode.isArray()).thenReturn(true);
		when(attachmentArrayNode.iterator()).thenReturn(List.of(attachmentNode).iterator());
		when(attachmentNode.get("contentType")).thenReturn(contentTypeNode);
		when(contentTypeNode.asText()).thenReturn("application/pdf");
		when(attachmentNode.get("name")).thenReturn(fileNameNode);
		when(fileNameNode.asText()).thenReturn("someFileName");

		var result = historyService.extractAttachment(history);

		assertThat(result).isNotNull().satisfies(attachments -> {
			assertThat(attachments).hasSize(1);
			assertThat(attachments.getFirst().contentType()).isEqualTo("application/pdf");
			assertThat(attachments.getFirst().fileName()).isEqualTo("someFileName");
		});
	}

	@Test
	void createRecipientTest() {
		var municipalityId = "2281";
		var history = HistoryEntity.builder()
			.withPartyId("partyId")
			.withMessageType(MessageType.MESSAGE)
			.withStatus(MessageStatus.SENT)
			.build();
		var histories = List.of(history);
		var expectedLegalId = "123456-7890";
		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, histories.getFirst().getPartyId())).thenReturn(expectedLegalId);

		var result = historyService.createRecipients(municipalityId, histories);

		assertThat(result).isNotNull().satisfies(recipients -> {
			assertThat(recipients).hasSize(1);
			assertThat(recipients.getFirst().personId()).isEqualTo(expectedLegalId);
			assertThat(recipients.getFirst().messageType()).isEqualTo(histories.getFirst().getMessageType().name());
		});

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, histories.getFirst().getPartyId());
		verifyNoMoreInteractions(partyIntegrationMock);
	}

	@Test
	void createRecipientTest_nullPartyId() {
		var municipalityId = "2281";
		var history = HistoryEntity.builder()
			.withMessageType(MessageType.MESSAGE)
			.withStatus(MessageStatus.SENT)
			.build();
		var histories = List.of(history);

		var result = historyService.createRecipients(municipalityId, histories);

		assertThat(result).isNotNull().satisfies(recipients -> {
			assertThat(recipients).hasSize(1);
			assertThat(recipients.getFirst().personId()).isNull();
			assertThat(recipients.getFirst().messageType()).isEqualTo(histories.getFirst().getMessageType().name());
		});

		verify(partyIntegrationMock, never()).getLegalIdByPartyId(eq(municipalityId), any());
		verifyNoMoreInteractions(partyIntegrationMock);
	}

	@Test
	void getUserMessageTest() throws JsonProcessingException {
		var municipalityId = "2281";
		var history = createHistoryEntity();
		var messageId = history.getMessageId();
		var issuer = history.getIssuer();
		var jsonNodeMock = mock(JsonNode.class);

		when(objectMapper.readTree(history.getContent())).thenReturn(jsonNodeMock);
		when(mockDbIntegration.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).thenReturn(true);
		when(mockDbIntegration.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(List.of(history));

		var result = historyService.getUserMessage(municipalityId, issuer, messageId);

		assertThat(result).isNotNull();
		assertThat(result.messageId()).isEqualTo(messageId);
		assertThat(result.issuer()).isEqualTo(issuer);
		assertThat(result.attachments()).isEmpty();
		assertThat(result.recipients()).satisfies(recipients -> {
			assertThat(recipients).hasSize(1);
			assertThat(recipients.getFirst().messageType()).isEqualTo(history.getMessageType().name());
			assertThat(recipients.getFirst().status()).isEqualTo(history.getStatus().name());
			assertThat(recipients.getFirst().address().address()).isEqualTo("someAddress");
			assertThat(recipients.getFirst().address().city()).isEqualTo("someCity");
			assertThat(recipients.getFirst().address().country()).isEqualTo("someCountry");
			assertThat(recipients.getFirst().address().firstName()).isEqualTo("someFirstName");
			assertThat(recipients.getFirst().address().lastName()).isEqualTo("someLastName");
			assertThat(recipients.getFirst().address().careOf()).isEqualTo("someCareOf");
			assertThat(recipients.getFirst().address().zipCode()).isEqualTo("12345");
		});

		verify(objectMapper, times(2)).readTree(history.getContent());
		verify(mockDbIntegration).existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer);
		verify(mockDbIntegration).getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, history.getPartyId());
		verifyNoInteractions(httpServletResponseMock);
		verifyNoMoreInteractions(objectMapper, mockDbIntegration, partyIntegrationMock);
	}

	@Test
	void getUserMessageTest_whenMessageIdNotFound() {
		var municipalityId = "2281";
		var messageId = "someMessageId";
		var issuer = "someIssuer";

		when(mockDbIntegration.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).thenReturn(false);

		assertThatThrownBy(() -> historyService.getUserMessage(municipalityId, issuer, messageId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: No message found for message id " + messageId + " and user id " + issuer);

		verify(mockDbIntegration).existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer);
		verifyNoInteractions(objectMapper, partyIntegrationMock, httpServletResponseMock);
		verifyNoMoreInteractions(mockDbIntegration);
	}
}
