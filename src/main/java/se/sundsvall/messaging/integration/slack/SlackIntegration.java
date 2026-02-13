package se.sundsvall.messaging.integration.slack;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.messaging.model.MessageStatus;

@Component
@EnableConfigurationProperties(SlackIntegrationProperties.class)
public class SlackIntegration {

	private final MethodsClient methodsClient;

	SlackIntegration(final MethodsClient methodsClient) {
		this.methodsClient = methodsClient;
	}

	public MessageStatus sendMessage(final SlackDto dto) {
		try {
			var request = ChatPostMessageRequest.builder()
				.token(dto.token())
				.channel(dto.channel())
				.blocks(List.of(
					SectionBlock.builder()
						.text(MarkdownTextObject.builder()
							.text(dto.message())
							.build())
						.build()))
				.build();

			var response = methodsClient.chatPostMessage(request);

			if (response.isOk()) {
				return SENT;
			}

			throw Problem.builder()
				.withStatus(BAD_GATEWAY)
				.withTitle("Unable to send Slack message")
				.withDetail(response.getError())
				.build();
		} catch (Exception e) {
			throw Problem.builder()
				.withStatus(BAD_GATEWAY)
				.withTitle("Unable to send Slack message")
				.withDetail(e.getMessage())
				.build();
		}
	}
}
