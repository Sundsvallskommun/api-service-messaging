package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.ReferenceType;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.digitalmailsender.Details;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceRequest;

@UnitTest
class DigitalMailSenderIntegrationMapperTest {

	private final DigitalMailSenderIntegrationMapper mapper = new DigitalMailSenderIntegrationMapper();

	@Test
	void test_toDigitalMailRequest_whenDtoIsNull() {
		assertThat(mapper.toDigitalMailRequest(null)).isNull();
	}

	@Test
	void test_toDigitalMailRequest() {
		var dto = DigitalMailDto.builder()
			.withSender(DigitalMailDto.Sender.builder()
				.withMunicipalityId("someMunicipalityId")
				.withSupportInfo(DigitalMailDto.Sender.SupportInfo.builder()
					.withEmailAddress("someEmailAddress")
					.withPhoneNumber("somePhoneNumber")
					.withText("someText")
					.withUrl("someUrl")
					.build())
				.build())
			.withPartyId("somePartyId")
			.withSubject("someSubject")
			.withContentType(ContentType.TEXT_PLAIN)
			.withBody("someBody")
			.withAttachments(List.of(DigitalMailDto.Attachment.builder()
				.withFilename("someFilename")
				.withContentType(ContentType.APPLICATION_PDF)
				.withContent("someContent")
				.build()))
			.build();

		var request = mapper.toDigitalMailRequest(dto);

		assertThat(request.getHeaderSubject()).isEqualTo("someSubject");
		assertThat(request.getMunicipalityId()).isEqualTo("someMunicipalityId");
		assertThat(request.getBodyInformation()).satisfies(bodyInformation -> {
			assertThat(bodyInformation.getContentType()).isEqualTo(ContentType.TEXT_PLAIN.getValue());
			assertThat(bodyInformation.getBody()).isEqualTo(dto.body());
		});
		assertThat(request.getAttachments()).hasSameSizeAs(dto.attachments());
		assertThat(request.getAttachments().getFirst()).satisfies(attachment -> {
			assertThat(attachment.getFilename()).isEqualTo("someFilename");
			assertThat(attachment.getContentType()).isEqualTo(ContentType.APPLICATION_PDF.getValue());
			assertThat(attachment.getBody()).isEqualTo("someContent");
		});
	}

	@Test
	void test_toDigitalInvoiceRequest_whenDtoIsNull() {
		assertThat(mapper.toDigitalInvoiceRequest(null)).isNull();
	}

	@Test
	void test_toDigitalInvoiceRequest() {
		var dto = DigitalInvoiceDto.builder()
			.withPartyId("somePartyId")
			.withType(InvoiceType.INVOICE)
			.withSubject("someSubject")
			.withReference("someReference")
			.withDetails(DigitalInvoiceDto.Details.builder()
				.withAmount(56.78f)
				.withDueDate(LocalDate.now().plusDays(7))
				.withPaymentReferenceType(ReferenceType.SE_OCR)
				.withPaymentReference("somePaymentReference")
				.withAccountType(AccountType.BANKGIRO)
				.withAccountNumber("someAccountNumber")
				.build())
			.withFiles(List.of(DigitalInvoiceDto.File.builder()
				.withContentType(ContentType.APPLICATION_PDF.getValue())
				.withContent("someContent")
				.withFilename("someFilename")
				.build()))
			.build();

		var request = mapper.toDigitalInvoiceRequest(dto);

		assertThat(request.getSubject()).isEqualTo("someSubject");
		assertThat(request.getPartyId()).isEqualTo("somePartyId");
		assertThat(request.getType()).isEqualTo(DigitalInvoiceRequest.TypeEnum.INVOICE);
		assertThat(request.getSubject()).isEqualTo("someSubject");
		assertThat(request.getReference()).isEqualTo("someReference");
		assertThat(request.getDetails()).satisfies(requestDetails -> {
			assertThat(requestDetails.getAmount()).isEqualTo(dto.details().amount());
			assertThat(requestDetails.getDueDate()).isEqualTo(dto.details().dueDate());
			assertThat(requestDetails.getPaymentReferenceType()).isEqualTo(Details.PaymentReferenceTypeEnum.SE_OCR);
			assertThat(requestDetails.getPaymentReference()).isEqualTo(dto.details().paymentReference());
			assertThat(requestDetails.getAccountType()).isEqualTo(Details.AccountTypeEnum.BANKGIRO);
			assertThat(requestDetails.getAccountNumber()).isEqualTo(dto.details().accountNumber());
		});
		assertThat(request.getFiles()).hasSameSizeAs(dto.files());
		assertThat(request.getFiles().getFirst()).satisfies(file -> {
			assertThat(file.getFilename()).isEqualTo("someFilename");
			assertThat(file.getContentType()).isEqualTo(ContentType.APPLICATION_PDF.getValue());
			assertThat(file.getBody()).isEqualTo("someContent");
		});
	}

}
