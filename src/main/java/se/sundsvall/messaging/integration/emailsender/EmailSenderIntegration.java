package se.sundsvall.messaging.integration.emailsender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.MessageOutcome;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@Component
@EnableConfigurationProperties(EmailSenderIntegrationProperties.class)
public class EmailSenderIntegration {

	static final String INTEGRATION_NAME = "EmailSender";

	private final EmailSenderIntegrationMapper mapper;

	private final EmailSenderClient client;

	EmailSenderIntegration(final EmailSenderClient client, final EmailSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageOutcome sendEmail(final String municipalityId, final EmailDto dto) {
		final var response = client.sendEmail(municipalityId, mapper.toSendEmailRequest(dto));
		return new MessageOutcome(response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT);
	}

}
