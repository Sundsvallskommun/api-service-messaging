package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageMapperTests {

    @Mock
    private Defaults mockDefaults;
    @Mock
    private Defaults.Sms mockSmsDefaults;
    @Mock
    private Defaults.Email mockEmailDefaults;
    @Mock
    private Defaults.DigitalMail mockDigitalMailDefaults;
    @Mock
    private Defaults.DigitalMail.SupportInfo mockDigitalMailSupportInfoDefaults;

    private MessageMapper messageMapper;

    @BeforeEach
    void setUp() {
        when(mockSmsDefaults.name()).thenReturn("SomeSender");
        when(mockDefaults.sms()).thenReturn(mockSmsDefaults);

        when(mockEmailDefaults.name()).thenReturn("Some Sender");
        when(mockEmailDefaults.address()).thenReturn("somesender@somehost.com");
        when(mockEmailDefaults.replyTo()).thenReturn("noreply@somehost.com");
        when(mockDefaults.email()).thenReturn(mockEmailDefaults);

        when(mockDigitalMailSupportInfoDefaults.text()).thenReturn("someText");
        when(mockDigitalMailSupportInfoDefaults.url()).thenReturn("someUrl");
        when(mockDigitalMailSupportInfoDefaults.emailAddress()).thenReturn("somesender@somehost.com");
        when(mockDigitalMailSupportInfoDefaults.phoneNumber()).thenReturn("060123456");
        when(mockDigitalMailDefaults.supportInfo()).thenReturn(mockDigitalMailSupportInfoDefaults);
        when(mockDigitalMailDefaults.municipalityId()).thenReturn("someMunicipalityId");
        when(mockDefaults.digitalMail()).thenReturn(mockDigitalMailDefaults);

        messageMapper = new MessageMapper(mockDefaults);
    }

    @Test
    void test_toMessage_withEmailRequest() {
        var request = createValidEmailRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.batchId()).isNull();
        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.type()).isEqualTo(MessageType.EMAIL);
        assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.content()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toMessage_withSnailMailRequest() {
        var request = createValidSnailMailRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.batchId()).isNull();
        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.type()).isEqualTo(MessageType.SNAIL_MAIL);
        assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.content()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toMessage_withSmsRequest() {
        var request = createValidSmsRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.batchId()).isNull();
        assertThat(message.type()).isEqualTo(MessageType.SMS);
        assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.content()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toMessage_withWebMessageRequest() {
        var request = createValidWebMessageRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.batchId()).isNull();
        assertThat(message.messageId()).isNotNull();
        assertThat(message.type()).isEqualTo(MessageType.WEB_MESSAGE);
        assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.content()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toMessages_withDigitalMailRequest() {
        var request = createValidDigitalMailRequest();

        var messages = messageMapper.toMessages(request, "someBatchId");

        assertThat(messages).hasSize(1);

        var message = messages.get(0);

        assertThat(message.batchId()).isEqualTo("someBatchId");
        assertThat(message.messageId()).isNotNull();
        assertThat(message.type()).isEqualTo(MessageType.DIGITAL_MAIL);
        assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.content()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toMessage_withBatchIdAndMessageRequest() {
        var batchId = UUID.randomUUID().toString();

        var request = createValidMessageRequest();

        var message = messageMapper.toMessage(batchId, request);

        assertThat(message.batchId()).isEqualTo(batchId);
        assertThat(message.messageId()).isNotNull();
        assertThat(message.type()).isEqualTo(MessageType.MESSAGE);
        assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.content()).isEqualTo(MessageMapper.GSON.toJson(request));
    }
}
