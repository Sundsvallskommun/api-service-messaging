package se.sundsvall.messaging.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.Priority;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.integration.rabbitmq.RabbitMapper.toSmsRequest;
import static se.sundsvall.messaging.integration.rabbitmq.TestFixtures.smsQueueMessage;

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
}
