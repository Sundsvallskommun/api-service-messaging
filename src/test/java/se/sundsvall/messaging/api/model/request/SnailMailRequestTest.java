package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.createAddress;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.SnailMailRequest.Attachment;
import se.sundsvall.messaging.api.model.request.SnailMailRequest.Party;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.ExternalReference;

class SnailMailRequestTest {

	private static final Party PARTY = Party.builder().build();
	private static final String DEPARTMENT = "department";
	private static final String DEVIATION = "deviation";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final List<Attachment> ATTACHMENTS = List.of(Attachment.builder().build());
	private static final String CONTENT = "content";
	private static final String CONTENT_TYPE = "contentType";
	private static final String NAME = "name";
	private static final String PARTY_ID = "partyId";
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final Address ADDRESS = createAddress();

	// SnailMailRequest
	@Test
	void testSnailMailRequestConstructor() {
		final var bean = new SnailMailRequest(PARTY, ADDRESS, DEPARTMENT, DEVIATION, ORIGIN, ISSUER, ATTACHMENTS, MUNICIPALITY_ID);

		assertSnailMailRequest(bean);
	}

	@Test
	void testSnailMailRequestBuilder() {
		final var bean = SnailMailRequest.builder()
			.withAttachments(ATTACHMENTS)
			.withDepartment(DEPARTMENT)
			.withDeviation(DEVIATION)
			.withIssuer(ISSUER)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withAddress(ADDRESS)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertSnailMailRequest(bean);
	}

	private void assertSnailMailRequest(final SnailMailRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.department()).isEqualTo(DEPARTMENT);
		assertThat(bean.deviation()).isEqualTo(DEVIATION);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean.address()).isEqualTo(ADDRESS);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// SnailMailRequest.Attachment
	@Test
	void testSnailMailRequestAttachmentConstructor() {
		final var bean = new SnailMailRequest.Attachment(NAME, CONTENT_TYPE, CONTENT);

		assertSnailMailRequestAttachment(bean);
	}

	@Test
	void testSnailMailRequestAttachmentBuilder() {
		final var bean = SnailMailRequest.Attachment.builder()
			.withContent(CONTENT)
			.withContentType(CONTENT_TYPE)
			.withFilename(NAME)
			.build();

		assertSnailMailRequestAttachment(bean);
	}

	private void assertSnailMailRequestAttachment(final Attachment bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.filename()).isEqualTo(NAME);
	}

	// SnailMailRequest.Party
	@Test
	void testSnailMailRequestPartyConstructor() {
		final var bean = new SnailMailRequest.Party(PARTY_ID, EXTERNAL_REFERENCES);

		assertSnailMailRequestParty(bean);
	}

	@Test
	void testSnailMailRequestPartyBuilder() {
		final var bean = SnailMailRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyId(PARTY_ID)
			.build();

		assertSnailMailRequestParty(bean);
	}

	private void assertSnailMailRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(SnailMailRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(SnailMailRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(SnailMailRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
	}
}
