package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.api.request.MessageRequest;
import se.sundsvall.messaging.api.response.BatchStatusResponse;
import se.sundsvall.messaging.api.response.HistoryResponse;
import se.sundsvall.messaging.api.response.MessageBatchResponse;
import se.sundsvall.messaging.api.response.MessageResponse;
import se.sundsvall.messaging.api.response.MessageStatusResponse;
import se.sundsvall.messaging.model.dto.EmailDto;
import se.sundsvall.messaging.model.dto.HistoryDto;
import se.sundsvall.messaging.model.dto.MessageBatchDto;
import se.sundsvall.messaging.model.dto.SmsDto;
import se.sundsvall.messaging.service.EmailService;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.service.SmsService;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();

    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;
    @Mock
    private MessageService messageService;
    @Mock
    private HistoryService historyService;

    private MessageController messageController;

    @BeforeEach
    void setUp() {
        messageController = new MessageController(emailService, smsService, messageService, historyService);
    }

    @Test
    void sendSms_whenValidSmsRequest_thenReturnOk() {
        SmsDto smsDto = SmsDto.builder()
                .withBatchId(BATCH_ID)
                .withMessageId(MESSAGE_ID)
                .withSender("sender")
                .withPartyId(PARTY_ID)
                .withMobileNumber("+46701234567")
                .withMessage("message")
                .withStatus(MessageStatus.SENT)
                .build();

        when(smsService.saveSms(any())).thenReturn(smsDto);

        ResponseEntity<MessageResponse> response = messageController.sendSms(createIncomingSmsRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessageId).isEqualTo(smsDto.getMessageId());

        verify(smsService, times(1)).saveSms(any());

    }

    @Test
    void sendEmail_givenValidEmailRequest_thenReturnOk() {
        EmailDto emailDto = EmailDto.builder()
                .withStatus(MessageStatus.SENT)
                .withAttachments(List.of())
                .withMessageId(MESSAGE_ID)
                .withSubject("subject")
                .withSenderName("sender name")
                .withMessage("message")
                .withSenderEmail("sender@hotmail.com")
                .withPartyId(PARTY_ID)
                .withEmailAddress("test@hotmail.com")
                .withBatchId(BATCH_ID)
                .build();

        when(emailService.saveEmail(any())).thenReturn(emailDto);

        ResponseEntity<MessageResponse> response = messageController.sendEmail(createIncomingEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessageId).isEqualTo(emailDto.getMessageId());

        verify(emailService, times(1)).saveEmail(any());

    }

    @Test
    void sendMessage_givenValidMessageRequest_thenReturnOK() {
        MessageBatchDto.Message messages = MessageBatchDto.Message.builder()
                .withSmsName("sms")
                .withMessageId(MESSAGE_ID)
                .withPartyId(PARTY_ID)
                .withMessage("message")
                .build();

        MessageBatchDto batchDto = MessageBatchDto.builder()
                .withMessages(List.of(messages))
                .withBatchId(BATCH_ID)
                .build();

        when(messageService.saveIncomingMessages(any())).thenReturn(batchDto);

        ResponseEntity<MessageBatchResponse> response = messageController.sendMessage(createMessageRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageBatchResponse::getBatchId)
                .isEqualTo(batchDto.getBatchId());

        verify(messageService, times(1)).saveIncomingMessages(any());
    }

    @Test
    void getMessageStatus_whenHistoryExistWithMessageId_thenResponseStatus_OK() {
        HistoryDto history = createHistoryDto();

        when(historyService.getHistoryByMessageId(anyString())).thenReturn(history);

        ResponseEntity<MessageStatusResponse> statusResponse = messageController.getMessageStatus(MESSAGE_ID);

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody())
                .matches(messageStatus -> Objects.equals(messageStatus.getMessageId(), MESSAGE_ID));
    }

    @Test
    void getMessageStatus_whenNoHistoryExist_thenThrowsProblem() {
        when(historyService.getHistoryByMessageId(anyString())).thenThrow(Problem.valueOf(Status.NOT_FOUND));

        assertThatThrownBy(() ->  messageController.getMessageStatus(MESSAGE_ID))
                .isInstanceOf(ThrowableProblem.class);
    }

    @Test
    void getBatchStatus_whenHistoryExistWithBatchId_thenResponseStatus_OK() {
        List<HistoryDto> historyEntries = List.of(createHistoryDto());

        when(historyService.getHistoryByBatchId(anyString())).thenReturn(historyEntries);

        ResponseEntity<BatchStatusResponse> statusResponse = messageController.getBatchStatus(BATCH_ID);

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody())
                .extracting(BatchStatusResponse::getMessageStatuses)
                .satisfies(messageStatuses -> assertThat(messageStatuses).hasSize(1));
    }

    @Test
    void getConversationHistory_whenHistoryExistsForPartyId_thenReturnOk() {
        when(historyService.getHistoryForPartyId(any())).thenReturn(List.of(createHistoryDto()));

        ResponseEntity<List<HistoryResponse>> response = messageController.getConversationHistory(PARTY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1)
                .allMatch(historyResponse -> Objects.equals(historyResponse.getPartyId(), PARTY_ID));
    }

    @Test
    void getConversationHistory_whenNoHistoryExistsForPartyId_thenReturnNoContent() {
        when(historyService.getHistoryForPartyId(any())).thenReturn(Collections.emptyList());

        ResponseEntity<List<HistoryResponse>> response = messageController.getConversationHistory("123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    private IncomingSmsRequest createIncomingSmsRequest() {
        return IncomingSmsRequest.builder()
                .withMessage("message")
                .withSender("sender")
                .withMobileNumber("+46701234567")
                .build();
    }

    private IncomingEmailRequest createIncomingEmailRequest() {
        return IncomingEmailRequest.builder()
                .withAttachments(List.of())
                .withEmailAddress("test@hotmail.com")
                .withHtmlMessage("message")
                .withMessage("message")
                .withSenderEmail("sender@hotmail.com")
                .withPartyId(PARTY_ID)
                .withSenderName("sender")
                .withSubject("subject")
                .build();
    }

    private HistoryDto createHistoryDto() {
        return HistoryDto.builder()
                .withBatchId(BATCH_ID)
                .withCreatedAt(LocalDateTime.now())
                .withPartyContact("party contact")
                .withMessageType(MessageType.SMS)
                .withMessage("message")
                .withMessageId(MESSAGE_ID)
                .withPartyId(PARTY_ID)
                .build();
    }

    private MessageRequest createMessageRequest() {
        return MessageRequest.builder()
                .withMessages(List.of(MessageRequest.Message.builder()
                        .withMessage("message")
                        .withEmailName("test@hotmail.com")
                        .withPartyId(PARTY_ID)
                        .withSenderEmail("test2@hotmail.com")
                        .withSubject("subject")
                        .build()))
                .build();
    }
}
