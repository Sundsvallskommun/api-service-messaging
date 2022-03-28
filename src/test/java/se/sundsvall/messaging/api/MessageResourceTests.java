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
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.api.model.BatchStatusResponse;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageBatchResponse;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.MessageResponse;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.dto.HistoryDto;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;
import se.sundsvall.messaging.service.EmailService;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.service.SmsService;
import se.sundsvall.messaging.service.WebMessageService;

@ExtendWith(MockitoExtension.class)
class MessageResourceTests {

    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();

    @Mock
    private EmailService mockEmailService;
    @Mock
    private SmsService mockSmsService;
    @Mock
    private WebMessageService mockWebMessageService;
    @Mock
    private MessageService mockMessageService;
    @Mock
    private HistoryService mockHistoryService;

    private MessageResource messageResource;

    @BeforeEach
    void setUp() {
        messageResource = new MessageResource(mockEmailService, mockSmsService,
            mockWebMessageService, mockMessageService, mockHistoryService);
    }

    @Test
    void sendSms_whenValidSmsRequest_thenReturnOk() {
        var smsDto = SmsDto.builder()
            .withBatchId(BATCH_ID)
            .withMessageId(MESSAGE_ID)
            .withSender("sender")
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .build())
            .withMobileNumber("+46701234567")
            .withMessage("message")
            .withStatus(MessageStatus.SENT)
            .build();

        when(mockSmsService.saveSms(any())).thenReturn(smsDto);

        var response = messageResource.sendSms(createIncomingSmsRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessageId).isEqualTo(smsDto.getMessageId());

        verify(mockSmsService, times(1)).saveSms(any());
    }

    @Test
    void sendEmail_givenValidEmailRequest_thenReturnOk() {
        var emailDto = EmailDto.builder()
            .withStatus(MessageStatus.SENT)
            .withAttachments(List.of())
            .withMessageId(MESSAGE_ID)
            .withSubject("subject")
            .withSenderName("sender name")
            .withMessage("message")
            .withSenderEmail("sender@hotmail.com")
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .build())
            .withEmailAddress("test@hotmail.com")
            .withBatchId(BATCH_ID)
            .build();

        when(mockEmailService.saveEmail(any())).thenReturn(emailDto);

        var response = messageResource.sendEmail(createIncomingEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessageId).isEqualTo(emailDto.getMessageId());

        verify(mockEmailService, times(1)).saveEmail(any());
    }

    @Test
    void sendMessage_givenValidMessageRequest_thenReturnOK() {
        var message = MessageBatchDto.Message.builder()
            .withSmsName("sms")
            .withMessageId(MESSAGE_ID)
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .build())
            .withMessage("message")
            .build();

        var batchDto = MessageBatchDto.builder()
            .withMessages(List.of(message))
            .withBatchId(BATCH_ID)
            .build();

        when(mockMessageService.saveIncomingMessages(any())).thenReturn(batchDto);

        var response = messageResource.sendMessage(createMessageRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageBatchResponse::getBatchId)
            .isEqualTo(batchDto.getBatchId());

        verify(mockMessageService, times(1)).saveIncomingMessages(any());
    }

    @Test
    void getMessageStatus_whenHistoryExistWithMessageId_thenResponseStatus_OK() {
        var historyDto = createHistoryDto();

        when(mockHistoryService.getHistoryByMessageId(anyString())).thenReturn(historyDto);

        var statusResponse = messageResource.getMessageStatus(MESSAGE_ID);

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody())
            .matches(messageStatus -> Objects.equals(messageStatus.getMessageId(), MESSAGE_ID));
    }

    @Test
    void getMessageStatus_whenNoHistoryExist_thenThrowsProblem() {
        when(mockHistoryService.getHistoryByMessageId(anyString())).thenThrow(Problem.valueOf(Status.NOT_FOUND));

        assertThatThrownBy(() ->  messageResource.getMessageStatus(MESSAGE_ID))
            .isInstanceOf(ThrowableProblem.class);
    }

    @Test
    void getBatchStatus_whenHistoryExistWithBatchId_thenResponseStatus_OK() {
        var historyDtos = List.of(createHistoryDto());

        when(mockHistoryService.getHistoryByBatchId(anyString())).thenReturn(historyDtos);

        var statusResponse = messageResource.getBatchStatus(BATCH_ID);

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody())
            .extracting(BatchStatusResponse::getMessageStatuses)
            .satisfies(messageStatuses -> assertThat(messageStatuses).hasSize(1));
    }

    @Test
    void getConversationHistory_whenHistoryExistsForPartyId_thenReturnOk() {
        when(mockHistoryService.getHistoryForPartyId(any())).thenReturn(List.of(createHistoryDto()));

        var response = messageResource.getConversationHistory(PARTY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1)
            .allMatch(historyResponse -> Objects.equals(historyResponse.getParty().getPartyId(), PARTY_ID));
    }

    @Test
    void getConversationHistory_whenNoHistoryExistsForPartyId_thenReturnNoContent() {
        when(mockHistoryService.getHistoryForPartyId(any())).thenReturn(Collections.emptyList());

        var response = messageResource.getConversationHistory("123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    private SmsRequest createIncomingSmsRequest() {
        return SmsRequest.builder()
            .withMessage("message")
            .withSender("sender")
            .withMobileNumber("+46701234567")
            .build();
    }

    private EmailRequest createIncomingEmailRequest() {
        return EmailRequest.builder()
            .withAttachments(List.of())
            .withEmailAddress("test@hotmail.com")
            .withHtmlMessage("message")
            .withMessage("message")
            .withSenderEmail("sender@hotmail.com")
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .build())
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
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .build())
            .build();
    }

    private MessageRequest createMessageRequest() {
        return MessageRequest.builder()
            .withMessages(List.of(MessageRequest.Message.builder()
                .withMessage("message")
                .withEmailName("test@hotmail.com")
                .withParty(Party.builder()
                    .withPartyId(PARTY_ID)
                    .build())
                .withSenderEmail("test2@hotmail.com")
                .withSubject("subject")
                .build()))
            .build();
    }
}
