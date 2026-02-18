package se.sundsvall.messaging.api.model.request;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest.Details;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest.File;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest.Party;
import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.ReferenceType;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalInvoiceRequestTest {

	private static final DigitalInvoiceRequest.Party PARTY = DigitalInvoiceRequest.Party.builder().build();
	private static final InvoiceType TYPE = InvoiceType.INVOICE;
	private static final String SUBJECT = "subject";
	private static final String REFERENCE = "reference";
	private static final boolean PAYABLE = true;
	private static final DigitalInvoiceRequest.Details DETAILS = DigitalInvoiceRequest.Details.builder().build();
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final List<DigitalInvoiceRequest.File> FILES = List.of(DigitalInvoiceRequest.File.builder().build());
	private static final AccountType ACCOUNT_TYPE = AccountType.BANKGIRO;
	private static final String ACCOUNT_NUMBER = "accountNumber";
	private static final float AMOUNT = 45.67f;
	private static final LocalDate DUE_DATE = LocalDate.now();
	private static final String PAYMENT_REFERENCE = "paymentReference";
	private static final ReferenceType PAYMENT_REFERENCE_TYPE = ReferenceType.SE_OCR;
	private static final String CONTENT = "content";
	private static final String CONTENT_TYPE = "contentType";
	private static final String FILENAME = "filename";
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final String PARTY_ID = "partyId";
	private static final String MUNICIPALITY_ID = "municipalityId";

	// DigitalInvoiceRequest
	@Test
	void testDigitalInvoiceRequestConstructor() {
		final var bean = new DigitalInvoiceRequest(PARTY, TYPE, SUBJECT, REFERENCE, PAYABLE, DETAILS, ORIGIN, ISSUER, FILES, MUNICIPALITY_ID);

		assertDigitalInvoiceRequest(bean);
	}

	@Test
	void testDigitalInvoiceRequestBuilder() {
		final var bean = DigitalInvoiceRequest.builder()
			.withDetails(DETAILS)
			.withFiles(FILES)
			.withIssuer(ISSUER)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withPayable(PAYABLE)
			.withReference(REFERENCE)
			.withSubject(SUBJECT)
			.withType(TYPE)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertDigitalInvoiceRequest(bean);
	}

	private void assertDigitalInvoiceRequest(final DigitalInvoiceRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.type()).isEqualTo(TYPE);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.reference()).isEqualTo(REFERENCE);
		assertThat(bean.payable()).isEqualTo(PAYABLE);
		assertThat(bean.details()).isEqualTo(DETAILS);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.files()).isEqualTo(FILES);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// DigitalInvoiceRequest.Details
	@Test
	void testDigitalInvoiceRequestDetailsConstructor() {
		final var bean = new DigitalInvoiceRequest.Details(AMOUNT, DUE_DATE, PAYMENT_REFERENCE_TYPE, PAYMENT_REFERENCE, ACCOUNT_TYPE, ACCOUNT_NUMBER);

		assertDigitalInvoiceRequestDetails(bean);
	}

	@Test
	void testDigitalInvoiceRequestDetailsBuilder() {
		final var bean = DigitalInvoiceRequest.Details.builder()
			.withAccountNumber(ACCOUNT_NUMBER)
			.withAccountType(ACCOUNT_TYPE)
			.withAmount(AMOUNT)
			.withDueDate(DUE_DATE)
			.withPaymentReference(PAYMENT_REFERENCE)
			.withPaymentReferenceType(PAYMENT_REFERENCE_TYPE)
			.build();

		assertDigitalInvoiceRequestDetails(bean);
	}

	private void assertDigitalInvoiceRequestDetails(final Details bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.accountNumber()).isEqualTo(ACCOUNT_NUMBER);
		assertThat(bean.accountType()).isEqualTo(ACCOUNT_TYPE);
		assertThat(bean.amount()).isEqualTo(AMOUNT);
		assertThat(bean.dueDate()).isEqualTo(DUE_DATE);
		assertThat(bean.paymentReference()).isEqualTo(PAYMENT_REFERENCE);
		assertThat(bean.paymentReferenceType()).isEqualTo(PAYMENT_REFERENCE_TYPE);
	}

	// DigitalInvoiceRequest.File
	@Test
	void testDigitalInvoiceRequestFileConstructor() {
		final var bean = new DigitalInvoiceRequest.File(CONTENT_TYPE, CONTENT, FILENAME);

		assertDigitalInvoiceRequestFile(bean);
	}

	@Test
	void testDigitalInvoiceRequestFileBuilder() {
		final var bean = DigitalInvoiceRequest.File.builder()
			.withContent(CONTENT)
			.withContentType(CONTENT_TYPE)
			.withFilename(FILENAME)
			.build();

		assertDigitalInvoiceRequestFile(bean);
	}

	private void assertDigitalInvoiceRequestFile(final File bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.filename()).isEqualTo(FILENAME);
	}

	// DigitalInvoiceRequest.Party
	@Test
	void testDigitalInvoiceRequestPartyConstructor() {
		final var bean = new DigitalInvoiceRequest.Party(PARTY_ID, EXTERNAL_REFERENCES);

		assertDigitalInvoiceRequestParty(bean);
	}

	@Test
	void testDigitalInvoiceRequestPartyBuilder() {
		final var bean = DigitalInvoiceRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyId(PARTY_ID)
			.build();

		assertDigitalInvoiceRequestParty(bean);
	}

	private void assertDigitalInvoiceRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(DigitalInvoiceRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalInvoiceRequest.Details.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalInvoiceRequest.File.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalInvoiceRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
	}
}
