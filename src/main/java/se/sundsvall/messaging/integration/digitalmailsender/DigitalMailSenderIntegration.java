package se.sundsvall.messaging.integration.digitalmailsender;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceResponse;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.model.MessageStatus;

@Component
@EnableConfigurationProperties(DigitalMailSenderIntegrationProperties.class)
public class DigitalMailSenderIntegration {

	static final String INTEGRATION_NAME = "DigitalMailSender";

	private final DigitalMailSenderClient client;

	private final DigitalMailSenderIntegrationMapper mapper;

	public DigitalMailSenderIntegration(final DigitalMailSenderClient client,
		final DigitalMailSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageStatus sendDigitalMail(final String municipalityId, final DigitalMailDto dto) {
		final var response = client.sendDigitalMail(municipalityId, mapper.toDigitalMailRequest(dto));

		final var success = response.getStatusCode().is2xxSuccessful() &&
			ofNullable(response.getBody())
				.map(DigitalMailResponse::getDeliveryStatus)
				.map(DeliveryStatus::getDelivered)
				.orElse(false);

		return success ? SENT : NOT_SENT;
	}

	public MessageStatus sendDigitalInvoice(final String municipalityId, final DigitalInvoiceDto dto) {
		final var response = client.sendDigitalInvoice(municipalityId, mapper.toDigitalInvoiceRequest(dto));

		final var success = response.getStatusCode().is2xxSuccessful() &&
			ofNullable(response.getBody())
				.map(DigitalInvoiceResponse::getSent)
				.orElse(false);

		return success ? SENT : NOT_SENT;
	}

}
