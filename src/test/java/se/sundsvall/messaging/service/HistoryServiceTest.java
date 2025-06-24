package se.sundsvall.messaging.service;

import static java.util.Collections.emptyList;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.messaging.TestDataFactory.createAttachment;
import static se.sundsvall.messaging.TestDataFactory.createHistoryEntity;
import static se.sundsvall.messaging.TestDataFactory.createUserMessage;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_INVOICE;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.hc.client5.http.utils.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import se.sundsvall.messaging.integration.db.projection.BatchHistoryProjection;
import se.sundsvall.messaging.integration.db.projection.MessageIdProjection;
import se.sundsvall.messaging.integration.party.PartyIntegration;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

	@Mock
	private DbIntegration dbIntegrationMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private HttpServletResponse httpServletResponseMock;

	@Mock
	private ObjectMapper objectMapperMock;

	@Mock
	private BatchExtractor batchExtractorMock;

	@Captor
	private ArgumentCaptor<PageRequest> pageRequestCaptor;

	@InjectMocks
	private HistoryService historyService;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(dbIntegrationMock, partyIntegrationMock, httpServletResponseMock, objectMapperMock, batchExtractorMock);
	}

	@Test
	void test_getHistoryByMunicipalityIdAndMessageId() {
		when(dbIntegrationMock.getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndMessageId("2281", "someMessageId");

		assertThat(result).isNotEmpty();

		verify(dbIntegrationMock).getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndMessageId_whenNoEntityExists() {
		when(dbIntegrationMock.getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of());

		final var result = historyService.getHistoryByMunicipalityIdAndMessageId("2281", "someMessageId");

		assertThat(result).isEmpty();

		verify(dbIntegrationMock).getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByBatchId() {
		when(dbIntegrationMock.getHistoryByMunicipalityIdAndBatchId(any(String.class), any(String.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndBatchId("2281", "someBatchId");

		assertThat(result).hasSize(1);

		verify(dbIntegrationMock).getHistoryByMunicipalityIdAndBatchId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndDeliveryId() {
		when(dbIntegrationMock.getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndDeliveryId("2281", "someBatchId");

		assertThat(result).isPresent();

		verify(dbIntegrationMock).getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndDeliveryId_whenNoEntityExists() {
		when(dbIntegrationMock.getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		final var result = historyService.getHistoryByMunicipalityIdAndDeliveryId("2281", "someBatchId");

		assertThat(result).isEmpty();

		verify(dbIntegrationMock).getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class));
	}

	@Test
	void test_getConversationHistory() {
		when(dbIntegrationMock.getHistory(any(String.class), any(String.class), nullable(LocalDate.class), nullable(LocalDate.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getConversationHistory("2281", "somePartyId", null, null);

		assertThat(result).hasSize(1);

		verify(dbIntegrationMock).getHistory(any(String.class), any(String.class), nullable(LocalDate.class), nullable(LocalDate.class));
	}

	@Test
	void streamAttachmentTest() throws IOException {
		final var municipalityId = "2281";
		final var messageId = "someMessageId";
		final var historyEntity = createHistoryEntity();
		final var attachment = createAttachment();
		final var spy = Mockito.spy(historyService);
		when(httpServletResponseMock.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		when(dbIntegrationMock.getFirstHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(historyEntity);
		doReturn(attachment).when(spy).findAttachmentByName(any(), any(), any());

		try (final MockedStatic<StreamUtils> streamMock = Mockito.mockStatic(StreamUtils.class)) {
			spy.streamAttachment(municipalityId, messageId, "someFileName", httpServletResponseMock);

			verify(httpServletResponseMock).addHeader(CONTENT_TYPE, attachment.getContentType());
			verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"");
			verify(httpServletResponseMock).addHeader(CONTENT_LENGTH, String.valueOf(Base64.decodeBase64(attachment.getContent()).length));
			verify(httpServletResponseMock).setContentLength(Base64.decodeBase64(attachment.getContent()).length);
			verify(objectMapperMock).readTree(historyEntity.getContent());
			streamMock.verify(() -> StreamUtils.copy(any(InputStream.class), any(OutputStream.class)));
		}
	}

	@Test
	void getAllUserMessagesTest() {
		final var municipalityId = "2281";
		final var userId = "someUserId";
		final var limit = 10;
		final var page = 1;
		final var userMessageList = List.of(createUserMessage(), createUserMessage());
		final var spy = Mockito.spy(historyService);
		final var messageIdProjectionMock = Mockito.mock(MessageIdProjection.class);
		final Page<MessageIdProjection> messageIdPage = new PageImpl<>(List.of(messageIdProjectionMock, messageIdProjectionMock, messageIdProjectionMock));
		when(dbIntegrationMock.getUniqueMessageIds(eq(municipalityId), eq(userId), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(messageIdPage);
		doReturn(userMessageList).when(spy).createUserMessages(municipalityId, messageIdPage.getContent());

		final var result = spy.getUserMessages(municipalityId, userId, null, page, limit);

		assertThat(result).isNotNull().satisfies(userMessages -> {
			assertThat(userMessages.messages()).isEqualTo(userMessageList);
			assertThat(userMessages.metaData().getPage()).isEqualTo(1);
			assertThat(userMessages.metaData().getLimit()).isEqualTo(3);
			assertThat(userMessages.metaData().getCount()).isEqualTo(3);
			assertThat(userMessages.metaData().getTotalRecords()).isEqualTo(3);
			assertThat(userMessages.metaData().getTotalPages()).isEqualTo(1);
		});
		verify(dbIntegrationMock).getUniqueMessageIds(eq(municipalityId), eq(userId), any(LocalDateTime.class), pageRequestCaptor.capture());
		final var pageRequest = pageRequestCaptor.getValue();
		assertThat(pageRequest.getPageNumber()).isZero();
		assertThat(pageRequest.getPageSize()).isEqualTo(limit);
	}

	@Test
	void getBatchUserMessagesTest() {
		final var municipalityId = "2281";
		final var batchId = "someBatchId";
		final var userId = "someUserId";
		final var limit = 10;
		final var page = 1;
		final var userMessageList = List.of(createUserMessage());
		final var spy = Mockito.spy(historyService);
		final var messageIdProjectionMock = Mockito.mock(MessageIdProjection.class);
		final Page<MessageIdProjection> messageIdPage = new PageImpl<>(List.of(messageIdProjectionMock, messageIdProjectionMock));
		when(dbIntegrationMock.getUniqueMessageIds(eq(municipalityId), eq(batchId), eq(userId), any(LocalDateTime.class), any(PageRequest.class))).thenReturn(messageIdPage);
		doReturn(userMessageList).when(spy).createUserMessages(municipalityId, messageIdPage.getContent());

		final var result = spy.getUserMessages(municipalityId, userId, batchId, page, limit);

		assertThat(result).isNotNull().satisfies(userMessages -> {
			assertThat(userMessages.messages()).isEqualTo(userMessageList);
			assertThat(userMessages.metaData().getPage()).isEqualTo(1);
			assertThat(userMessages.metaData().getLimit()).isEqualTo(2);
			assertThat(userMessages.metaData().getCount()).isEqualTo(2);
			assertThat(userMessages.metaData().getTotalRecords()).isEqualTo(2);
			assertThat(userMessages.metaData().getTotalPages()).isEqualTo(1);
		});
		verify(dbIntegrationMock).getUniqueMessageIds(eq(municipalityId), eq(batchId), eq(userId), any(LocalDateTime.class), pageRequestCaptor.capture());
		final var pageRequest = pageRequestCaptor.getValue();
		assertThat(pageRequest.getPageNumber()).isZero();
		assertThat(pageRequest.getPageSize()).isEqualTo(limit);
	}

	@Test
	void createUserMessagesTest() {
		final var municipalityId = "2281";
		final var messageIdProjectionMock = Mockito.mock(MessageIdProjection.class);
		final var spy = Mockito.spy(historyService);
		when(messageIdProjectionMock.getMessageId()).thenReturn("1");
		doReturn(createUserMessage()).when(spy).createUserMessage(eq(municipalityId), any());

		final var result = spy.createUserMessages(municipalityId, List.of(messageIdProjectionMock, messageIdProjectionMock, messageIdProjectionMock));

		assertThat(result).isNotNull().hasSize(3);
	}

	@Test
	void createUserMessageTest() {
		final var municipalityId = "2281";
		final var messageId = "someMessageId";
		final var histories = List.of(createHistoryEntity());
		final var spy = Mockito.spy(historyService);
		when(dbIntegrationMock.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(histories);
		final var subject = "someSubject";
		doReturn(subject).when(spy).extractSubject(histories.getFirst());
		final var recipients = List.of(UserMessage.Recipient.builder().withMessageType("SNAIL_MAIL").withPersonId("123456-7890"));
		doReturn(recipients).when(spy).createRecipients(municipalityId, histories);
		final var attachments = List.of(UserMessage.MessageAttachment.builder().withContentType("application/pdf").withFileName("someFileName").build());
		doReturn(attachments).when(spy).extractAttachment(histories.getFirst());

		final var result = spy.createUserMessage(municipalityId, messageId);

		assertThat(result).isNotNull().satisfies(userMessage -> {
			assertThat(userMessage.messageId()).isEqualTo(messageId);
			assertThat(userMessage.recipients()).isEqualTo(recipients);
			assertThat(userMessage.attachments()).isEqualTo(attachments);
			assertThat(userMessage.issuer()).isEqualTo(histories.getFirst().getIssuer());
			assertThat(userMessage.origin()).isEqualTo(histories.getFirst().getOrigin());
			assertThat(userMessage.sent()).isEqualTo(histories.getFirst().getCreatedAt());
		});

		verify(dbIntegrationMock).getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		verify(spy).createRecipients(municipalityId, histories);
		verify(spy).extractAttachment(histories.getFirst());
		verify(spy).createUserMessage(municipalityId, messageId);
		verifyNoMoreInteractions(spy);
	}

	@Test
	void findAttachmentByNameTest() {
		final var contentInputNode = mock(JsonNode.class);
		final var attachmentArrayNode = mock(JsonNode.class);
		final var attachmentNode = mock(JsonNode.class);

		final var fileNameNode = mock(JsonNode.class);
		final var contentTypeNode = mock(JsonNode.class);
		final var contentNode = mock(JsonNode.class);

		when(contentInputNode.get("attachments")).thenReturn(attachmentArrayNode);
		when(attachmentArrayNode.isArray()).thenReturn(true);
		when(attachmentArrayNode.iterator()).thenReturn(List.of(attachmentNode).iterator());
		when(attachmentNode.get("name")).thenReturn(fileNameNode);
		when(attachmentNode.get("content")).thenReturn(contentNode);
		when(attachmentNode.get("contentType")).thenReturn(contentTypeNode);
		when(fileNameNode.asText()).thenReturn("someFileName");
		when(contentTypeNode.asText()).thenReturn("application/pdf");
		when(contentNode.asText()).thenReturn("someContent");

		final var result = historyService.findAttachmentByName(contentInputNode, "name", "someFileName");

		assertThat(result).isNotNull().satisfies(attachment -> {
			assertThat(attachment.getName()).isEqualTo("someFileName");
			assertThat(attachment.getContentType()).isEqualTo("application/pdf");
			assertThat(attachment.getContent()).isEqualTo("someContent");
		});
	}

	@Test
	void findAttachmentByNameTest_2() {
		final var contentInputNode = mock(JsonNode.class);
		final var attachmentArrayNode = mock(JsonNode.class);

		when(contentInputNode.get("attachments")).thenReturn(attachmentArrayNode);
		when(attachmentArrayNode.isArray()).thenReturn(false);

		assertThatThrownBy(() -> historyService.findAttachmentByName(contentInputNode, "name", "someFileName"))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Attachment with name someFileName not found");
	}

	@Test
	void extractAttachmentTest() throws JsonProcessingException {
		final var history = createHistoryEntity();

		final var jsonNodeMock = mock(JsonNode.class);
		final var attachmentArrayNode = mock(JsonNode.class);
		final var attachmentNode = mock(JsonNode.class);
		final var fileNameNode = mock(JsonNode.class);
		final var contentTypeNode = mock(JsonNode.class);

		when(objectMapperMock.readTree(history.getContent())).thenReturn(jsonNodeMock);
		when(jsonNodeMock.get("attachments")).thenReturn(attachmentArrayNode);
		when(attachmentArrayNode.isArray()).thenReturn(true);
		when(attachmentArrayNode.iterator()).thenReturn(List.of(attachmentNode).iterator());
		when(attachmentNode.get("contentType")).thenReturn(contentTypeNode);
		when(contentTypeNode.asText()).thenReturn("application/pdf");
		when(attachmentNode.get("filename")).thenReturn(fileNameNode);
		when(fileNameNode.asText()).thenReturn("someFileName");

		final var result = historyService.extractAttachment(history);

		assertThat(result).isNotNull().satisfies(attachments -> {
			assertThat(attachments).hasSize(1);
			assertThat(attachments.getFirst().contentType()).isEqualTo("application/pdf");
			assertThat(attachments.getFirst().fileName()).isEqualTo("someFileName");
		});
	}

	@Test
	void createRecipientTest() {
		final var municipalityId = "2281";
		final var history = HistoryEntity.builder()
			.withPartyId("partyId")
			.withMessageType(MessageType.MESSAGE)
			.withStatus(MessageStatus.SENT)
			.build();
		final var histories = List.of(history);
		final var expectedLegalId = "123456-7890";
		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, histories.getFirst().getPartyId())).thenReturn(expectedLegalId);

		final var result = historyService.createRecipients(municipalityId, histories);

		assertThat(result).isNotNull().satisfies(recipients -> {
			assertThat(recipients).hasSize(1);
			assertThat(recipients.getFirst().personId()).isEqualTo(expectedLegalId);
			assertThat(recipients.getFirst().messageType()).isEqualTo(histories.getFirst().getMessageType().name());
		});

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, histories.getFirst().getPartyId());
	}

	@Test
	void createRecipientTest_nullPartyId() {
		final var municipalityId = "2281";
		final var history = HistoryEntity.builder()
			.withMessageType(MESSAGE)
			.withStatus(MessageStatus.SENT)
			.build();
		final var histories = List.of(history);

		final var result = historyService.createRecipients(municipalityId, histories);

		assertThat(result).isNotNull().satisfies(recipients -> {
			assertThat(recipients).hasSize(1);
			assertThat(recipients.getFirst().personId()).isNull();
			assertThat(recipients.getFirst().messageType()).isEqualTo(histories.getFirst().getMessageType().name());
		});

		verify(partyIntegrationMock, never()).getLegalIdByPartyId(eq(municipalityId), any());
	}

	@Test
	void getUserMessageTest() throws JsonProcessingException {
		final var municipalityId = "2281";
		final var history = createHistoryEntity();
		final var messageId = history.getMessageId();
		final var issuer = history.getIssuer();
		final var jsonNodeMock = mock(JsonNode.class);

		when(objectMapperMock.readTree(history.getContent())).thenReturn(jsonNodeMock);
		when(dbIntegrationMock.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).thenReturn(true);
		when(dbIntegrationMock.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(List.of(history));

		final var result = historyService.getUserMessage(municipalityId, issuer, messageId);

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

		verify(objectMapperMock, times(2)).readTree(history.getContent());
		verify(dbIntegrationMock).existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer);
		verify(dbIntegrationMock).getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, history.getPartyId());
	}

	@Test
	void getUserMessageTest_whenMessageIdNotFound() {
		final var municipalityId = "2281";
		final var messageId = "someMessageId";
		final var issuer = "someIssuer";

		when(dbIntegrationMock.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).thenReturn(false);

		assertThatThrownBy(() -> historyService.getUserMessage(municipalityId, issuer, messageId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: No message found for message id " + messageId + " and user id " + issuer);

		verify(dbIntegrationMock).existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer);
	}

	@Test
	void getUserBatches() {
		final var municipalityId = "municipalityId";
		final var issuer = "issuer";
		final var page = 1;
		final var limit = 1;
		final var date = LocalDate.now().minusDays(30).atStartOfDay();
		final var message1batch1 = createBatchHistoryProjection("messageId1", "batchId1");
		final var message2batch1 = createBatchHistoryProjection("messageId2", "batchId1");
		final var message1batch2 = createBatchHistoryProjection("messageId3", "batchId2");
		final var message2batch2 = createBatchHistoryProjection("messageId4", "batchId2");

		final var projections = List.of(
			message1batch1,
			message2batch1,
			message1batch2,
			message2batch2);

		when(dbIntegrationMock.getBatchHistoryMessagesForUser(municipalityId, issuer, date)).thenReturn(projections);

		final var bean = historyService.getUserBatches(municipalityId, issuer, page, limit);

		verify(dbIntegrationMock).getBatchHistoryMessagesForUser(municipalityId, issuer, date);
		verify(batchExtractorMock).extractAttachmentCount(municipalityId, List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractAttachmentCount(municipalityId, List.of(message1batch2, message2batch2));
		verify(batchExtractorMock).extractOriginalMesageType(List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractOriginalMesageType(List.of(message1batch2, message2batch2));
		verify(batchExtractorMock).extractRecipientCount(List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractRecipientCount(List.of(message1batch2, message2batch2));
		verify(batchExtractorMock).extractSent(List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractSent(List.of(message1batch2, message2batch2));
		verify(batchExtractorMock).extractSubject(municipalityId, List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractSubject(municipalityId, List.of(message1batch2, message2batch2));
		verify(batchExtractorMock).extractSuccessfulCount(List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractSuccessfulCount(List.of(message1batch2, message2batch2));
		verify(batchExtractorMock).extractUnsuccessfulCount(List.of(message1batch1, message2batch1));
		verify(batchExtractorMock).extractUnsuccessfulCount(List.of(message1batch2, message2batch2));

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.batches()).hasSize(1);
		assertThat(bean.metaData().getCount()).isEqualTo(page);
		assertThat(bean.metaData().getLimit()).isEqualTo(limit);
		assertThat(bean.metaData().getPage()).isEqualTo(page);
		assertThat(bean.metaData().getTotalPages()).isEqualTo(2);
		assertThat(bean.metaData().getTotalRecords()).isEqualTo(2);
	}

	@Test
	void getUserBatches_whenNoMatch() {
		final var municipalityId = "municipalityId";
		final var issuer = "issuer";
		final var page = 123;
		final var limit = 321;
		final var date = LocalDate.now().minusDays(30).atStartOfDay();

		when(dbIntegrationMock.getBatchHistoryMessagesForUser(municipalityId, issuer, date)).thenReturn(emptyList());

		final var bean = historyService.getUserBatches(municipalityId, issuer, page, limit);

		verify(dbIntegrationMock).getBatchHistoryMessagesForUser(municipalityId, issuer, date);
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.batches()).isEmpty();
		assertThat(bean.metaData().getCount()).isZero();
		assertThat(bean.metaData().getLimit()).isEqualTo(limit);
		assertThat(bean.metaData().getPage()).isEqualTo(page);
		assertThat(bean.metaData().getTotalPages()).isZero();
		assertThat(bean.metaData().getTotalRecords()).isZero();
	}

	@Test
	void createBatch() {
		final var municipalityId = "municipalityId";
		final var batchId = "batchId";
		final var messageId1 = "messageId1";
		final var messageId2 = "messageId2";
		final var projections = List.of(createBatchHistoryProjection(messageId1), createBatchHistoryProjection(messageId2));
		final var entry = Map.entry(batchId, projections);

		final var bean = historyService.createBatch(municipalityId, entry);

		assertThat(bean).isNotNull().hasAllNullFieldsOrPropertiesExcept("batchId", "attachmentCount", "recipientCount", "status");
		assertThat(bean.batchId()).isEqualTo(batchId);
		assertThat(bean.attachmentCount()).isZero();
		assertThat(bean.recipientCount()).isZero();
		assertThat(bean.status().successful()).isZero();
		assertThat(bean.status().unsuccessful()).isZero();

		verify(batchExtractorMock).extractAttachmentCount(municipalityId, projections);
		verify(batchExtractorMock).extractOriginalMesageType(projections);
		verify(batchExtractorMock).extractRecipientCount(projections);
		verify(batchExtractorMock).extractSent(projections);
		verify(batchExtractorMock).extractSubject(municipalityId, projections);
		verify(batchExtractorMock).extractSuccessfulCount(projections);
		verify(batchExtractorMock).extractUnsuccessfulCount(projections);

	}

	@Test
	void createBatch_fromNull() {
		assertThat(historyService.createBatch(null, null)).isNull();
	}

	@ParameterizedTest
	@MethodSource("attachmentFieldProvider")
	void testGetAttachmentField(MessageType messageType, String attachmentFieldName) {
		assertThat(historyService.getAttachmentsField(messageType)).isEqualTo(attachmentFieldName);
	}

	@ParameterizedTest
	@MethodSource("fileNameFieldProvider")
	void testGetFileNameField(MessageType messageType, String attachmentFieldName) {
		assertThat(historyService.getFileNameField(messageType)).isEqualTo(attachmentFieldName);
	}

	@ParameterizedTest
	@MethodSource("contentTypeFieldProvider")
	void testGetContentTypeField(MessageType messageType, String attachmentFieldName) {
		assertThat(historyService.getContentTypeField(messageType)).isEqualTo(attachmentFieldName);
	}

	public static Stream<Arguments> attachmentFieldProvider() {
		return Stream.of(
			Arguments.of(MESSAGE, null),
			Arguments.of(EMAIL, "attachments"),
			Arguments.of(SMS, null),
			Arguments.of(WEB_MESSAGE, "attachments"),
			Arguments.of(DIGITAL_MAIL, "attachments"),
			Arguments.of(DIGITAL_INVOICE, "files"),
			Arguments.of(SNAIL_MAIL, "attachments"),
			Arguments.of(LETTER, "attachments"),
			Arguments.of(SLACK, null));
	}

	public static Stream<Arguments> fileNameFieldProvider() {
		return Stream.of(
			Arguments.of(MESSAGE, null),
			Arguments.of(EMAIL, "name"),
			Arguments.of(SMS, null),
			Arguments.of(WEB_MESSAGE, "fileName"),
			Arguments.of(DIGITAL_MAIL, "filename"),
			Arguments.of(DIGITAL_INVOICE, "filename"),
			Arguments.of(SNAIL_MAIL, "filename"),
			Arguments.of(LETTER, "filename"),
			Arguments.of(SLACK, null));
	}

	public static Stream<Arguments> contentTypeFieldProvider() {
		return Stream.of(
			Arguments.of(MESSAGE, null),
			Arguments.of(EMAIL, "contentType"),
			Arguments.of(SMS, null),
			Arguments.of(WEB_MESSAGE, "mimeType"),
			Arguments.of(DIGITAL_MAIL, "contentType"),
			Arguments.of(DIGITAL_INVOICE, "contentType"),
			Arguments.of(SNAIL_MAIL, "contentType"),
			Arguments.of(LETTER, "contentType"),
			Arguments.of(SLACK, null));
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId) {
		return createBatchHistoryProjection(messageId, null);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId, String batchId) {
		return new BatchHistoryProjection() {

			@Override
			public MessageStatus getStatus() {
				return null;
			}

			@Override
			public MessageType getOriginalMessageType() {
				return null;
			}

			@Override
			public MessageType getMessageType() {
				return null;
			}

			@Override
			public String getMessageId() {
				return messageId;
			}

			@Override
			public LocalDateTime getCreatedAt() {
				return null;
			}

			@Override
			public String getBatchId() {
				return batchId;
			}
		};
	}
}
