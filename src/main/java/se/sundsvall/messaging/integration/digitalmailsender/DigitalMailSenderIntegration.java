package se.sundsvall.messaging.integration.digitalmailsender;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceResponse;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.response.Mailbox;
import se.sundsvall.messaging.model.MessageOutcome;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@Component
@EnableConfigurationProperties(DigitalMailSenderIntegrationProperties.class)
public class DigitalMailSenderIntegration {

	static final String INTEGRATION_NAME = "DigitalMailSender";

	private final DigitalMailSenderClient client;

	private final DigitalMailSenderIntegrationMapper mapper;

	DigitalMailSenderIntegration(final DigitalMailSenderClient client,
		final DigitalMailSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageOutcome sendDigitalMail(final String municipalityId, final String organizationNumber, final DigitalMailDto dto) {
		final var response = client.sendDigitalMail(municipalityId, organizationNumber, mapper.toDigitalMailRequest(dto));

		final var body = ofNullable(response.getBody());
		final var deliveryStatus = body.map(DigitalMailResponse::getDeliveryStatus);

		final var success = response.getStatusCode().is2xxSuccessful() &&
			deliveryStatus.map(DeliveryStatus::getDelivered).orElse(false);

		final var transactionId = deliveryStatus.map(DeliveryStatus::getTransactionId).orElse(null);

		return new MessageOutcome(success ? SENT : NOT_SENT, transactionId);
	}

	public MessageOutcome sendDigitalInvoice(final String municipalityId, final DigitalInvoiceDto dto) {
		final var response = client.sendDigitalInvoice(municipalityId, mapper.toDigitalInvoiceRequest(dto));

		final var success = response.getStatusCode().is2xxSuccessful() &&
			ofNullable(response.getBody())
				.map(DigitalInvoiceResponse::getSent)
				.orElse(false);

		return new MessageOutcome(success ? SENT : NOT_SENT);
	}

	public List<Mailbox> getMailboxes(final String municipalityId, final String organizationNumber, final List<String> partyIds) {
		var response = client.getMailboxes(municipalityId, organizationNumber, partyIds);

		return mapper.toMailboxes(response.getBody());
	}
}
