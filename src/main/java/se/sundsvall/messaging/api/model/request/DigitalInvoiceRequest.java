package se.sundsvall.messaging.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;
import lombok.With;

import se.sundsvall.dept44.common.validators.annotation.OneOf;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.ReferenceType;

import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@With
@Builder(setterPrefix = "with")
public record DigitalInvoiceRequest(

	@Valid
	@NotNull
	@Schema(description = "Party", requiredMode = REQUIRED)
	Party party,

	@NotNull
	@Schema(description = "Invoice type", requiredMode = REQUIRED)
	InvoiceType type,

	@Schema(description = "Subject", nullable = true)
	String subject,

	@Schema(description = "Invoice reference", example = "Faktura #12345")
	String reference,

	@Schema(description = "Whether the invoice is payable", defaultValue = "true")
	Boolean payable,

	@NotNull
	@Valid
	@Schema(requiredMode = REQUIRED)
	Details details,

	@Schema(description = "Origin of request", example = "web", hidden = true)
	@JsonIgnore
	String origin,

	@Schema(description = "Files")
	List<@Valid File> files) {

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "DigitalInvoiceParty")
	public record Party(

		@ValidUuid
        @Schema(description = "The recipient party id", format = "uuid")
		String partyId,

		@Schema(description = "External references")
		List<@Valid ExternalReference> externalReferences) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(description = "Invoice details")
	public record Details(

		@NotNull
		@Positive
		@Schema(description = "The invoice amount", example = "123.45", requiredMode = REQUIRED)
		Float amount,

		@NotNull
		@Schema(description = "The invoice due date", example = "2023-10-09", requiredMode = REQUIRED)
		LocalDate dueDate,

		@NotNull
		@Schema(requiredMode = REQUIRED)
		ReferenceType paymentReferenceType,

		@NotBlank
		@Schema(description = "The payment reference number", maxLength = 25, example = "426523791", requiredMode = REQUIRED)
		String paymentReference,

		@NotNull
		@Schema(requiredMode = REQUIRED)
		AccountType accountType,

		@NotBlank
		@Schema(description = "The receiving account (a valid BANKGIRO or PLUSGIRO number)", example = "12345", requiredMode = REQUIRED)
		String accountNumber) {
	}

	@With
	@Builder(setterPrefix = "with")
	@Schema(name = "DigitalInvoiceFile")
	public record File(

		@OneOf(APPLICATION_PDF_VALUE)
		@Schema(description = "Content type", allowableValues = {APPLICATION_PDF_VALUE}, requiredMode = REQUIRED)
		String contentType,

		@ValidBase64
		@Schema(description = "Content (BASE64-encoded)", requiredMode = REQUIRED)
		String content,

		@NotBlank
		@Schema(description = "Filename", requiredMode = REQUIRED)
		String filename) {
	}
}
