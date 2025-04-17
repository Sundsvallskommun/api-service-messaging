package se.sundsvall.messaging.service.mapper;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalInvoiceDto;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailDto;
import se.sundsvall.messaging.integration.emailsender.EmailDto;
import se.sundsvall.messaging.integration.oepintegrator.WebMessageDto;
import se.sundsvall.messaging.integration.slack.SlackDto;
import se.sundsvall.messaging.integration.smssender.SmsDto;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailDto;
import se.sundsvall.messaging.model.ContentType;

@Component
public class DtoMapper {

	private final String defaultSmsDtoSender;
	private final EmailDto.Sender defaultEmailDtoSender;
	private final String defaultDigitalMailDtoMunicipalityId;
	private final DigitalMailDto.Sender.SupportInfo defaultDigitalMailDtoSenderSupportInfo;

	public DtoMapper(final Defaults defaults) {
		defaultSmsDtoSender = defaults.sms().name();

		defaultEmailDtoSender = EmailDto.Sender.builder()
			.withName(defaults.email().name())
			.withAddress(defaults.email().address())
			.withReplyTo(defaults.email().replyTo())
			.build();

		defaultDigitalMailDtoMunicipalityId = defaults.digitalMail().municipalityId();
		defaultDigitalMailDtoSenderSupportInfo = DigitalMailDto.Sender.SupportInfo.builder()
			.withText(defaults.digitalMail().supportInfo().text())
			.withEmailAddress(defaults.digitalMail().supportInfo().emailAddress())
			.withPhoneNumber(defaults.digitalMail().supportInfo().phoneNumber())
			.withUrl(defaults.digitalMail().supportInfo().url())
			.build();
	}

	public SmsDto toSmsDto(final SmsRequest request) {
		return SmsDto.builder()
			.withSender(ofNullable(request.sender())
				.orElse(defaultSmsDtoSender))
			.withMobileNumber(request.mobileNumber())
			.withMessage(request.message())
			.withPriority(ofNullable(request.priority()).orElse(Priority.NORMAL))
			.build();
	}

	public EmailDto toEmailDto(final EmailRequest request) {
		return EmailDto.builder()
			.withSender(ofNullable(request.sender())
				.map(requestSender -> EmailDto.Sender.builder()
					.withName(requestSender.name())
					.withAddress(requestSender.address())
					.withReplyTo(requestSender.replyTo())
					.build())
				.orElse(defaultEmailDtoSender))
			.withEmailAddress(request.emailAddress())
			.withSubject(request.subject())
			.withMessage(request.message())
			.withHtmlMessage(request.htmlMessage())
			.withAttachments(ofNullable(request.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> EmailDto.Attachment.builder()
						.withName(attachment.name())
						.withContentType(attachment.contentType())
						.withContent(attachment.content())
						.build())
					.toList())
				.orElse(null))
			.withHeaders(ofNullable(request.headers()).orElse(Map.of()).entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue)))
			.build();
	}

	public DigitalMailDto toDigitalMailDto(final DigitalMailRequest request, final String partyId) {
		return DigitalMailDto.builder()
			.withSender(DigitalMailDto.Sender.builder()
				.withMunicipalityId(defaultDigitalMailDtoMunicipalityId)
				.withSupportInfo(ofNullable(request.sender()).map(DigitalMailRequest.Sender::supportInfo)
					.map(supportInfo -> DigitalMailDto.Sender.SupportInfo.builder()
						.withText(supportInfo.text())
						.withEmailAddress(supportInfo.emailAddress())
						.withPhoneNumber(supportInfo.phoneNumber())
						.withUrl(supportInfo.url())
						.build())
					.orElse(defaultDigitalMailDtoSenderSupportInfo))
				.build())
			.withPartyId(partyId)
			.withContentType(ContentType.fromString(request.contentType()))
			.withSubject(request.subject())
			.withBody(request.body())
			.withAttachments(ofNullable(request.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> DigitalMailDto.Attachment.builder()
						.withFilename(attachment.filename())
						.withContentType(ContentType.fromString(attachment.contentType()))
						.withContent(attachment.content())
						.build())
					.toList())
				.orElse(null))
			.build();
	}

	public DigitalInvoiceDto toDigitalInvoiceDto(final DigitalInvoiceRequest request) {
		return DigitalInvoiceDto.builder()
			.withPartyId(request.party().partyId())
			.withType(request.type())
			.withSubject(request.subject())
			.withReference(request.reference())
			.withPayable(request.payable())
			.withDetails(DigitalInvoiceDto.Details.builder()
				.withAmount(request.details().amount())
				.withDueDate(request.details().dueDate())
				.withPaymentReferenceType(request.details().paymentReferenceType())
				.withPaymentReference(request.details().paymentReference())
				.withAccountType(request.details().accountType())
				.withAccountNumber(request.details().accountNumber())
				.build())
			.withFiles(request.files().stream()
				.map(file -> DigitalInvoiceDto.File.builder()
					.withFilename(file.filename())
					.withContentType(file.contentType())
					.withContent(file.content())
					.build())
				.toList())
			.build();
	}

	public WebMessageDto toWebMessageDto(final WebMessageRequest request) {
		return WebMessageDto.builder()
			.withPartyId(ofNullable(request.party())
				.map(WebMessageRequest.Party::partyId)
				.orElse(null))
			.withUserId(ofNullable(request.sender())
				.map(WebMessageRequest.Sender::userId)
				.orElse(null))
			.withExternalReferences(ofNullable(request.party())
				.map(WebMessageRequest.Party::externalReferences)
				.orElse(null))
			.withMessage(request.message())
			.withOepInstance(Optional.ofNullable(request.oepInstance())
				.orElse("external"))
			.withSendAsOwner(request.sendAsOwner())
			.withAttachments(ofNullable(request.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> WebMessageDto.Attachment.builder()
						.withFileName(attachment.fileName())
						.withMimeType(attachment.mimeType())
						.withBase64Data(attachment.base64Data())
						.build())
					.toList())
				.orElse(null))
			.build();
	}

	public SnailMailDto toSnailMailDto(final SnailMailRequest request, String batchId) {
		return SnailMailDto.builder()
			.withPartyId(request.party().partyId())
			.withAddress(request.address())
			.withBatchId(batchId)
			.withDepartment(request.department())
			.withDeviation(request.deviation())
			.withIssuer(request.issuer())
			.withOrigin(request.origin())
			.withAttachments(ofNullable(request.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> SnailMailDto.Attachment.builder()
						.withName(attachment.name())
						.withContentType(attachment.contentType())
						.withContent(attachment.content())
						.build())
					.toList())
				.orElse(null))
			.build();
	}

	public SlackDto toSlackDto(final SlackRequest request) {
		return SlackDto.builder()
			.withToken(request.token())
			.withChannel(request.channel())
			.withMessage(request.message())
			.build();
	}

}
