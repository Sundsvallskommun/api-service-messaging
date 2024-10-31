package se.sundsvall.messaging.integration.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SlackIntegrationTest {

	private final SlackDto slackDto = new SlackDto("someToken", "someChannel", "someMessage");

	@Mock
	private MethodsClient mockMethodsClient;

	@InjectMocks
	private SlackIntegration slackIntegration;

	@Test
	void testSendMessage() throws Exception {
		var response = new ChatPostMessageResponse();
		response.setOk(true);

		when(mockMethodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(response);

		var status = slackIntegration.sendMessage(slackDto);

		assertThat(status).isEqualTo(SENT);

		verify(mockMethodsClient, times(1)).chatPostMessage(any(ChatPostMessageRequest.class));
	}

	@Test
	void testSendMessage_failure() throws Exception {
		var response = new ChatPostMessageResponse();
		response.setOk(false);
		response.setError("Channel not found");

		when(mockMethodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(response);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> slackIntegration.sendMessage(slackDto));

		verify(mockMethodsClient, times(1)).chatPostMessage(any(ChatPostMessageRequest.class));
	}

	@Test
	void testSendMessage_whenExceptionIsThrown() throws Exception {
		var response = new ChatPostMessageResponse();
		response.setOk(false);
		response.setError("Channel not found");

		when(mockMethodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
			.thenThrow(new RuntimeException());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> slackIntegration.sendMessage(slackDto));

		verify(mockMethodsClient, times(1)).chatPostMessage(any(ChatPostMessageRequest.class));
	}
}
