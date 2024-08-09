package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageEventHandlerTest {

	private final IncomingMessageEvent event = new IncomingMessageEvent(this, "2281", EMAIL, "someDeliveryId", "someOrigin");

	@Mock
	private MessageService mockMessageService;

	@Mock
	private DbIntegration mockDbIntegration;

	@InjectMocks
	private MessageEventHandler messageEventHandler;

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void test_handleIncomingMessageEvent(final MessageType messageType) {
		when(mockDbIntegration.getMessageByDeliveryId(any(String.class)))
			.thenReturn(Optional.of(Message.builder().withType(messageType).build()));

		messageEventHandler.handleIncomingMessageEvent(event);

		verify(mockDbIntegration).getMessageByDeliveryId(any(String.class));

		if (messageType == MESSAGE) {
			verify(mockMessageService).sendMessage(any(Message.class), any(String.class));
		} else if (messageType == LETTER) {
			verify(mockMessageService).sendLetter(any(Message.class), any(String.class));
		} else {
			verify(mockMessageService).deliver(any(Message.class), any(String.class));
		}
		verifyNoMoreInteractions(mockMessageService);
	}

	@Test
	void test_handleIncomingMessageEventWhenMessageIsNotFound() {
		when(mockDbIntegration.getMessageByDeliveryId(any(String.class))).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> messageEventHandler.handleIncomingMessageEvent(event))
			.withMessageStartingWith("Internal Server Error: Unable to send");

		verify(mockDbIntegration).getMessageByDeliveryId(any(String.class));
		verifyNoInteractions(mockMessageService);
	}

}
