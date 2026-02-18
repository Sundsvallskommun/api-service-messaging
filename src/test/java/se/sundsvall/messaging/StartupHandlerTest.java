package se.sundsvall.messaging;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

@ExtendWith(MockitoExtension.class)
class StartupHandlerTest {

	@Mock
	private ApplicationEventPublisher mockEventPublisher;
	@Mock
	private DbIntegration mockDbIntegration;

	private StartupHandler startupProcessor;

	@BeforeEach
	void setUp() {
		startupProcessor = new StartupHandler(mockEventPublisher, mockDbIntegration);
	}

	@Test
	void testRun_whenNoPendingMessagesExist() {
		when(mockDbIntegration.getLatestMessagesWithStatus(PENDING)).thenReturn(List.of());

		startupProcessor.run();

		verify(mockDbIntegration, times(1)).getLatestMessagesWithStatus(PENDING);
		verify(mockEventPublisher, never()).publishEvent(any());
	}

	@Test
	void testRun() {
		var messages = List.of(
			MessageEntity.builder()
				.withMessageId("messageId1")
				.withType(MESSAGE)
				.build(),
			MessageEntity.builder()
				.withMessageId("messageId2")
				.withType(EMAIL)
				.build(),
			MessageEntity.builder()
				.withMessageId("messageId3")
				.withType(SMS)
				.build(),
			MessageEntity.builder()
				.withMessageId("messageId4")
				.withType(WEB_MESSAGE)
				.build(),
			MessageEntity.builder()
				.withMessageId("messageId4")
				.withType(SNAIL_MAIL)
				.build(),
			MessageEntity.builder()
				.withMessageId("messageId4")
				.withType(LETTER)
				.build(),
			MessageEntity.builder()
				.withMessageId("messageId4")
				.withType(DIGITAL_MAIL)
				.build());

		when(mockDbIntegration.getLatestMessagesWithStatus(PENDING)).thenReturn(messages);

		startupProcessor.run();

		verify(mockDbIntegration, times(1)).getLatestMessagesWithStatus(PENDING);
		verify(mockEventPublisher, times(7)).publishEvent(any(IncomingMessageEvent.class));
	}
}
