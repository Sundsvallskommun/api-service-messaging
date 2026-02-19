package se.sundsvall.messaging.api.model.request;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.SmsRequest.Party;
import se.sundsvall.messaging.model.ExternalReference;

import static org.assertj.core.api.Assertions.assertThat;

class SmsRequestTest {

	private static final Party PARTY = Party.builder().build();
	private static final String SENDER = "sender";
	private static final String MOBILE_NUMBER = "mobileNumber";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final String MESSAGE = "message";
	private static final Priority PRIORITY = Priority.HIGH;
	private static final String PARTY_ID = "partyId";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String DEPARTMENT = "department";
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());

	// SmsRequest
	@Test
	void testSmsRequestConstructor() {
		final var bean = new SmsRequest(PARTY, SENDER, MOBILE_NUMBER, ORIGIN, ISSUER, MESSAGE, PRIORITY, DEPARTMENT, MUNICIPALITY_ID);

		assertSmsRequest(bean);
	}

	@Test
	void testSmsRequestBuilder() {
		final var bean = SmsRequest.builder()
			.withIssuer(ISSUER)
			.withMessage(MESSAGE)
			.withMobileNumber(MOBILE_NUMBER)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withPriority(PRIORITY)
			.withSender(SENDER)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withDepartment(DEPARTMENT)
			.build();

		assertSmsRequest(bean);
	}

	private void assertSmsRequest(final SmsRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.mobileNumber()).isEqualTo(MOBILE_NUMBER);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.priority()).isEqualTo(PRIORITY);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(bean.department()).isEqualTo(DEPARTMENT);
	}

	// SmsRequest
	@Test
	void testSmsRequestPartyConstructor() {
		final var bean = new SmsRequest.Party(PARTY_ID, EXTERNAL_REFERENCES);

		assertSmsRequestParty(bean);
	}

	@Test
	void testSmsRequestPartyBuilder() {
		final var bean = SmsRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyId(PARTY_ID)
			.build();

		assertSmsRequestParty(bean);
	}

	private void assertSmsRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(SmsRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(SmsRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
	}
}
