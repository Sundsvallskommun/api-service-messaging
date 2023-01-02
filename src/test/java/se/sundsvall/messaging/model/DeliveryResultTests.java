package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DeliveryResultTests {

    @Mock
    private Message mockMessage;

    @Test
    void testDefaultConstructor() {
        var deliveryResult = new DeliveryResult("someMessageId", "someDeliveryId", MessageStatus.FAILED);

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.status()).isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void testConstructorAcceptingMessageEntity() {
        when(mockMessage.messageId()).thenReturn("someMessageId");
        when(mockMessage.deliveryId()).thenReturn("someDeliveryId");
        when(mockMessage.status()).thenReturn(MessageStatus.PENDING);

        var deliveryResult = new DeliveryResult(mockMessage);

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.status()).isEqualTo(MessageStatus.PENDING);

        verify(mockMessage, times(1)).messageId();
        verify(mockMessage, times(1)).deliveryId();
        verify(mockMessage, times(1)).status();
    }

    @Test
    void testConstructorAcceptingMessageEntityAndStatus() {
        when(mockMessage.messageId()).thenReturn("someMessageId");
        when(mockMessage.deliveryId()).thenReturn("someDeliveryId");

        var deliveryResult = new DeliveryResult(mockMessage, MessageStatus.SENT);

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.status()).isEqualTo(MessageStatus.SENT);

        verify(mockMessage, times(1)).messageId();
        verify(mockMessage, times(1)).deliveryId();
        verify(mockMessage, never()).status();
    }

    @Test
    void testConstructorAcceptingMessageId() {
        var deliveryResult = new DeliveryResult("someMessageId");

        assertThat(deliveryResult.messageId()).isEqualTo("someMessageId");
        assertThat(deliveryResult.deliveryId()).isNull();
        assertThat(deliveryResult.status()).isNull();
    }
}
