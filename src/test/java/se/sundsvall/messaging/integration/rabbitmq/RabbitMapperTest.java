package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.Priority;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toDigitalMailRequest;
import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toSmsRequest;
import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toSnailMailRequest;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.OBJECT_ID;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.digitalMailQueueMessage;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.smsQueueMessage;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.snailMailQueueMessage;

class RabbitMapperTest {

	@Test
	void toSmsRequest_mapsTheContract() {
		final var result = toSmsRequest(smsQueueMessage());

		assertThat(result.municipalityId()).isEqualTo("2281");
		assertThat(result.party().partyId()).isEqualTo("97edca90-7fa8-457e-8223-aa078055465c");
		assertThat(result.mobileNumber()).isEqualTo("+46701740605");
		assertThat(result.sender()).isEqualTo("Sundsvall");
		assertThat(result.department()).isEqualTo("Kommunstyrelsekontoret");
		assertThat(result.message()).isEqualTo("This is the message to be sent");
		// Reduced to the bare username, which is what history.issuer holds and what user history is filtered by.
		assertThat(result.issuer()).isEqualTo("mar14han");
		assertThat(result.origin()).isEqualTo("PostPortalService");
		assertThat(result.priority()).isEqualTo(Priority.NORMAL);
	}

	@Test
	void toIssuer_unwrapsAnAdAccount() {
		assertThat(RabbitMapper.toIssuer("mar14han; type=adAccount")).isEqualTo("mar14han");
	}

	@Test
	void toIssuer_unwrapsAPartyId() {
		// The value is not always a username, which is why the type travels with it and is dropped only here.
		assertThat(RabbitMapper.toIssuer("97edca90-7fa8-457e-8223-aa078055465c; type=partyId"))
			.isEqualTo("97edca90-7fa8-457e-8223-aa078055465c");
	}

	@Test
	void toIssuer_leavesABareValueAlone() {
		assertThat(RabbitMapper.toIssuer("mar14han")).isEqualTo("mar14han");
	}

	@Test
	void toIssuer_null() {
		assertThat(RabbitMapper.toIssuer(null)).isNull();
	}

	@Test
	void toSmsRequest_null() {
		assertThat(toSmsRequest(null)).isNull();
	}

	@Test
	void toDigitalMailRequest_mapsTheContract() {
		final var result = toDigitalMailRequest(digitalMailQueueMessage());

		assertThat(result.municipalityId()).isEqualTo("2281");
		// One party per queued request, held in the list the REST endpoint's shape requires.
		assertThat(result.party().partyIds()).containsExactly("97edca90-7fa8-457e-8223-aa078055465c");
		assertThat(result.subject()).isEqualTo("This is the subject");
		assertThat(result.body()).isEqualTo("This is the body");
		assertThat(result.contentType()).isEqualTo("text/plain");
		assertThat(result.department()).isEqualTo("Kommunstyrelsekontoret");
		assertThat(result.sender().supportInfo().text()).isEqualTo("Support text");
		assertThat(result.sender().supportInfo().emailAddress()).isEqualTo("support@sundsvall.se");
		assertThat(result.sender().supportInfo().phoneNumber()).isEqualTo("+46701740605");
		assertThat(result.sender().supportInfo().url()).isEqualTo("https://sundsvall.se/support");
		assertThat(result.issuer()).isEqualTo("mar14han");
		assertThat(result.origin()).isEqualTo("PostPortalService");
	}

	@Test
	void toDigitalMailRequest_attachmentsKeepTheirReferences() {
		// Resolution belongs in the delivery attempt, not here - fetching at map time would put the object store
		// outside the retry ladder.
		final var attachment = toDigitalMailRequest(digitalMailQueueMessage()).attachments().getFirst();

		assertThat(attachment.filename()).isEqualTo("file.pdf");
		assertThat(attachment.contentType()).isEqualTo("application/pdf");
		assertThat(attachment.objectId()).isEqualTo(OBJECT_ID);
		assertThat(attachment.content()).isNull();
	}

	@Test
	void toDigitalMailRequest_null() {
		assertThat(toDigitalMailRequest(null)).isNull();
	}

	@Test
	void toSnailMailRequest_mapsTheContract() {
		final var result = toSnailMailRequest(snailMailQueueMessage());

		assertThat(result.municipalityId()).isEqualTo("2281");
		assertThat(result.party().partyId()).isEqualTo("97edca90-7fa8-457e-8223-aa078055465c");
		assertThat(result.department()).isEqualTo("Kommunstyrelsekontoret");
		assertThat(result.deviation()).isEqualTo("A3 Ritning");
		assertThat(result.folderName()).isEqualTo("Sundsvalls Kommun");
		assertThat(result.issuer()).isEqualTo("mar14han");
		assertThat(result.origin()).isEqualTo("PostPortalService");
	}

	@Test
	void toSnailMailRequest_addressIsCarriedFieldForField() {
		final var address = toSnailMailRequest(snailMailQueueMessage()).address();

		assertThat(address).hasNoNullFieldsOrProperties();
		assertThat(address.firstName()).isEqualTo("John");
		assertThat(address.lastName()).isEqualTo("Doe");
		assertThat(address.organizationName()).isEqualTo("Acme AB");
		assertThat(address.address()).isEqualTo("Main Street 1");
		assertThat(address.apartmentNumber()).isEqualTo("1101");
		assertThat(address.careOf()).isEqualTo("c/o Jane Doe");
		assertThat(address.zipCode()).isEqualTo("12345");
		assertThat(address.city()).isEqualTo("Sundsvall");
		assertThat(address.country()).isEqualTo("Sweden");
	}

	@Test
	void toSnailMailRequest_addressIsOptional() {
		// A recipient reached by party id alone carries none; snailmail-sender looks the address up itself.
		final var message = snailMailQueueMessage();
		final var withoutAddress = new SnailMailQueueMessage(message.municipalityId(), message.messageId(), message.recipientId(),
			message.batchId(), message.partyId(), message.department(), message.folderName(), message.deviation(),
			null, message.attachments(), message.sentBy(), message.origin());

		assertThat(toSnailMailRequest(withoutAddress).address()).isNull();
	}

	@Test
	void toSnailMailRequest_attachmentsKeepTheirReferences() {
		final var attachment = toSnailMailRequest(snailMailQueueMessage()).attachments().getFirst();

		assertThat(attachment.filename()).isEqualTo("file.pdf");
		assertThat(attachment.contentType()).isEqualTo("application/pdf");
		assertThat(attachment.objectId()).isEqualTo(OBJECT_ID);
		assertThat(attachment.content()).isNull();
	}

	@Test
	void toSnailMailRequest_null() {
		assertThat(toSnailMailRequest(null)).isNull();
	}
}
