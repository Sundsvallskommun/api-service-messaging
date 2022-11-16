package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createMessageRequest;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createSnailmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createWebMessageRequest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.SnailmailRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;
import se.sundsvall.messaging.service.event.IncomingSnailmailEvent;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;

@ExtendWith(MockitoExtension.class)
class MessageServiceTests {

    @Mock
    private ApplicationEventPublisher mockEventPublisher;
    @Mock
    private MessageRepository mockRepository;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private MessageMapper mockMapper;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(mockEventPublisher, mockRepository, mockMapper);
    }

    @Test
    void test_handleMessageRequest() {
        when(mockRepository.save(any(MessageEntity.class)))
            .thenReturn(MessageEntity.builder()
                .withMessageId("someMessageId1")
                .build())
            .thenReturn(MessageEntity.builder()
                .withMessageId("someMessageId2")
                .build());

        var request = MessageRequest.builder()
            .withMessages(List.of(createMessageRequest(), createMessageRequest()))
            .build();

        var dto = messageService.handleMessageRequest(request);

        assertThat(dto.getBatchId()).isNotNull();
        assertThat(dto.getMessageIds()).hasSize(2);

        verify(mockMapper, times(2)).toEntity(any(String.class), any(MessageRequest.Message.class));
        verify(mockRepository, times(2)).save(any(MessageEntity.class));
        verify(mockEventPublisher, times(2)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_handleEmailRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
            .withMessageId("someMessageId")
            .build());

        var request = createEmailRequest();

        var dto = messageService.handleEmailRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(EmailRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingEmailEvent.class));
    }

    @Test
    void test_handleSmsRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
            .withMessageId("someMessageId")
            .build());

        var request = createSmsRequest();

        var dto = messageService.handleSmsRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(SmsRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingSmsEvent.class));
    }

    @Test
    void test_handleWebMessageRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
            .withMessageId("someMessageId")
            .build());

        var request = createWebMessageRequest();

        var dto = messageService.handleWebMessageRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(WebMessageRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingWebMessageEvent.class));
    }

    @Test
    void test_handleDigitalMailRequest() {
        var message = MessageEntity.builder()
            .withMessageId("someMessageId")
            .build();

        when(mockRepository.saveAll(Mockito.<List<MessageEntity>>any())).thenReturn(List.of(message));

        var request = createDigitalMailRequest();

        var dto = messageService.handleDigitalMailRequest(request);

        assertThat(dto.getMessageIds()).contains("someMessageId");

        verify(mockRepository, times(1)).saveAll(Mockito.<List<MessageEntity>>any());
        verify(mockMapper, times(1)).toEntities(any(DigitalMailRequest.class), any(String.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingDigitalMailEvent.class));
    }

    @Test
    void test_handleSnailmailRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
                .withMessageId("someMessageId")
                .build());

        var request = createSnailmailRequest();

        var dto = messageService.handleSnailmailRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(SnailmailRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingSnailmailEvent.class));
    }
}
