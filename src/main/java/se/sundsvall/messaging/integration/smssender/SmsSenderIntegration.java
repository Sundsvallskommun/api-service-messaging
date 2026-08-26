package se.sundsvall.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.integration.rabbitmq.SmsQueuePublisher;
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

	private final ObjectProvider<SmsQueuePublisher> smsQueuePublisher;

	SmsSenderIntegration(final SmsSenderClient client, final SmsSenderIntegrationMapper mapper, final ObjectProvider<SmsQueuePublisher> smsQueuePublisher) {
		this.client = client;
		this.mapper = mapper;
		this.smsQueuePublisher = smsQueuePublisher;
	}

	public MessageOutcome sendSms(final String municipalityId, final SmsDto dto) {
		// Mirror the outgoing SMS onto the queue (fire-and-forget; absent when disabled)
		ofNullable(smsQueuePublisher.getIfAvailable())
			.ifPresent(publisher -> publisher.publish(municipalityId, dto));

		final var response = client.sendSms(municipalityId, mapper.toSendSmsRequest(dto));

		final var success = response.getStatusCode().is2xxSuccessful() &&
			ofNullable(response.getBody())
				.map(SendSmsResponse::getSent)
				.orElse(false);

		return new MessageOutcome(success ? SENT : NOT_SENT);
	}

}
