package se.sundsvall.messaging.integration.emailsender;

import static se.sundsvall.messaging.integration.emailsender.EmailSenderIntegrationMapper.toSendEmailRequest;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.model.MessageStatus;

@Component
@EnableConfigurationProperties(EmailSenderIntegrationProperties.class)
public class EmailSenderIntegration {

	static final String INTEGRATION_NAME = "EmailSender";

	private final EmailSenderClient client;

	public EmailSenderIntegration(final EmailSenderClient client) {
		this.client = client;
	}

	public MessageStatus sendEmail(final EmailDto dto) {
		var response = client.sendEmail(toSendEmailRequest(dto));
		return response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT;
	}
}
