package se.sundsvall.messaging.integration.digitalmailsender;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.ReferenceType;

@Builder(setterPrefix = "with")
public record DigitalInvoiceDto(
	String partyId,
	InvoiceType type,
	String subject,
	String reference,
	Boolean payable,
	Details details,
	List<File> files) {

	@Builder(setterPrefix = "with")
	public record Details(
		Float amount,
		LocalDate dueDate,
		ReferenceType paymentReferenceType,
		String paymentReference,
		AccountType accountType,
		String accountNumber) {}

	@Builder(setterPrefix = "with")
	public record File(
		String filename,
		String contentType,
		String content) {}
}
