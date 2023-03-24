package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InternalDeliveryResultTests {

    @Mock
    private Message mockMessage;

    @Test
    void testDefaultConstructor() {
        var deliveryResult = new InternalDeliveryResult("someMessageId", "someDeliveryId", EMAIL, FAILED);

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.messageType()).isEqualTo(EMAIL);
        assertThat(deliveryResult.status()).isEqualTo(FAILED);
    }

    @Test
    void testConstructorAcceptingMessageEntity() {
        when(mockMessage.messageId()).thenReturn("someMessageId");
        when(mockMessage.deliveryId()).thenReturn("someDeliveryId");
        when(mockMessage.status()).thenReturn(PENDING);

        var deliveryResult = new InternalDeliveryResult(mockMessage);

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.status()).isEqualTo(PENDING);

        verify(mockMessage, times(1)).messageId();
        verify(mockMessage, times(1)).deliveryId();
        verify(mockMessage, times(1)).status();
    }

    @Test
    void testConstructorAcceptingMessageEntityAndStatus() {
        when(mockMessage.messageId()).thenReturn("someMessageId");
        when(mockMessage.deliveryId()).thenReturn("someDeliveryId");

        var deliveryResult = new InternalDeliveryResult(mockMessage, SENT);

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.status()).isEqualTo(SENT);

        verify(mockMessage, times(1)).messageId();
        verify(mockMessage, times(1)).deliveryId();
        verify(mockMessage, never()).status();
    }

    @Test
    void testConstructorAcceptingMessageId() {
        var deliveryResult = new InternalDeliveryResult("someMessageId");

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isNull();
        assertThat(deliveryResult.status()).isNull();
    }
}
