package se.sundsvall.messaging.integration.snailmailsender;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.MessageStatus;

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

	public MessageStatus sendSnailMail(final String municipalityId, final SnailMailDto dto) {
		var response = client.sendSnailmail(municipalityId, mapper.toSendSnailmailRequest(dto));

		return response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT;
	}

	public void sendBatch(final String municipalityId, final String batchId) {
		client.sendBatch(municipalityId, batchId);
	}

}
