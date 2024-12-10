package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest.Party;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsBatchRequestTest {

	private static final String SENDER = "sender";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final String MESSAGE = "message";
	private static final Priority PRIORITY = Priority.HIGH;
	private static final List<Party> PARTIES = List.of(Party.builder().build());
	private static final String PARTY_ID = "partyId";
	private static final String MOBILE_NUMBER = "mobileNumber";
	private static final String MUNICIPALITY_ID = "municipalityId";

	// SmsBatchRequest
	@Test
	void testSmsBatchRequestConstructor() {
		final var bean = new SmsBatchRequest(SENDER, ORIGIN, ISSUER, MESSAGE, PRIORITY, PARTIES, MUNICIPALITY_ID);

		assertSmsBatchRequest(bean);
	}

	@Test
	void testSmsBatchRequestBuilder() {
		final var bean = SmsBatchRequest.builder()
			.withIssuer(ISSUER)
			.withMessage(MESSAGE)
			.withOrigin(ORIGIN)
			.withParties(PARTIES)
			.withPriority(PRIORITY)
			.withSender(SENDER)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertSmsBatchRequest(bean);
	}

	private void assertSmsBatchRequest(final SmsBatchRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.parties()).isEqualTo(PARTIES);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.priority()).isEqualTo(PRIORITY);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// SmsBatchRequest.Party
	@Test
	void testSmsBatchRequestPartyConstructor() {
		final var bean = new SmsBatchRequest.Party(PARTY_ID, MOBILE_NUMBER);

		assertSmsBatchRequestParty(bean);
	}

	@Test
	void testSmsBatchRequestPartyBuilder() {
		final var bean = SmsBatchRequest.Party.builder()
			.withMobileNumber(MOBILE_NUMBER)
			.withPartyId(PARTY_ID)
			.build();

		assertSmsBatchRequestParty(bean);
	}

	private void assertSmsBatchRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.mobileNumber()).isEqualTo(MOBILE_NUMBER);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(SmsBatchRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(SmsBatchRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
	}
}
