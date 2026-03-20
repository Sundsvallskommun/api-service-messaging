package se.sundsvall.messaging.integration.snailmailsender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.MessageOutcome;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@Component
@EnableConfigurationProperties(SnailMailSenderIntegrationProperties.class)
public class SnailMailSenderIntegration {

	static final String INTEGRATION_NAME = "SnailmailSender";

	private final SnailMailSenderClient client;

	private final SnailMailSenderIntegrationMapper mapper;

	SnailMailSenderIntegration(final SnailMailSenderClient client, final SnailMailSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageOutcome sendSnailMail(final String municipalityId, final SnailMailDto dto) {
		final var response = client.sendSnailmail(dto.sentBy(), dto.sentBy(), dto.origin(), municipalityId, mapper.toSendSnailmailRequest(dto));

		return new MessageOutcome(response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT);
	}

	public void sendBatch(final String municipalityId, final String batchId) {
		client.sendBatch(municipalityId, batchId);
	}

}
