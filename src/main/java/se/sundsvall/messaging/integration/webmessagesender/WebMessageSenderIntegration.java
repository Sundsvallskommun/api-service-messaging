package se.sundsvall.messaging.integration.webmessagesender;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.model.MessageStatus;

@Component
@EnableConfigurationProperties(WebMessageSenderIntegrationProperties.class)
public class WebMessageSenderIntegration {

	static final String INTEGRATION_NAME = "WebMessageSender";

	private final WebMessageSenderClient client;

	private final WebMessageSenderIntegrationMapper mapper;

	WebMessageSenderIntegration(final WebMessageSenderClient client,
		final WebMessageSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageStatus sendWebMessage(final String municipalityId, final WebMessageDto dto) {
		final var response = client.sendWebMessage(municipalityId, mapper.toCreateWebMessageRequest(dto));

		return response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT;
	}

}
