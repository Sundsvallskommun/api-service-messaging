package se.sundsvall.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.MessageOutcome;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@Component
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
public class SmsSenderIntegration {

	static final String INTEGRATION_NAME = "SmsSender";

	private final SmsSenderClient client;

	private final SmsSenderIntegrationMapper mapper;

	SmsSenderIntegration(final SmsSenderClient client, final SmsSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageOutcome sendSms(final String municipalityId, final SmsDto dto) {
		final var response = client.sendSms(municipalityId, mapper.toSendSmsRequest(dto));

		final var success = response.getStatusCode().is2xxSuccessful() &&
			ofNullable(response.getBody())
				.map(SendSmsResponse::getSent)
				.orElse(false);

		return new MessageOutcome(success ? SENT : NOT_SENT);
	}

}
