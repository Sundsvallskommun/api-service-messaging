package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createMessageRequest;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createWebMessageRequest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;
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
    void test_saveMessageRequest() {
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

        var dto = messageService.saveMessageRequest(request);

        assertThat(dto.getBatchId()).isNotNull();
        assertThat(dto.getMessageIds()).hasSize(2);

        verify(mockMapper, times(2)).toEntity(any(String.class), any(MessageRequest.Message.class));
        verify(mockRepository, times(2)).save(any(MessageEntity.class));
        verify(mockMapper, times(2)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(2)).publishEvent(any(IncomingMessageEvent.class));
    }

    @Test
    void test_saveEmailRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
            .withMessageId("someMessageId")
            .build());

        var request = createEmailRequest();

        var dto = messageService.saveEmailRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(EmailRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingEmailEvent.class));
    }

    @Test
    void test_saveSmsRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
            .withMessageId("someMessageId")
            .build());

        var request = createSmsRequest();

        var dto = messageService.saveSmsRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(SmsRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingSmsEvent.class));
    }

    @Test
    void test_saveWebMessageRequest() {
        when(mockRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder()
            .withMessageId("someMessageId")
            .build());

        var request = createWebMessageRequest();

        var dto = messageService.saveWebMessageRequest(request);

        assertThat(dto.getMessageId()).isEqualTo("someMessageId");

        verify(mockRepository, times(1)).save(any(MessageEntity.class));
        verify(mockMapper, times(1)).toEntity(any(WebMessageRequest.class));
        verify(mockMapper, times(1)).toMessageDto(any(MessageEntity.class));
        verify(mockEventPublisher, times(1)).publishEvent(any(IncomingWebMessageEvent.class));
    }
}
