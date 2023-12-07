package se.sundsvall.messaging.integration.digitalmailsender;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.digitalmailsender.Attachment;
import generated.se.sundsvall.digitalmailsender.BodyInformation;
import generated.se.sundsvall.digitalmailsender.Details;
import generated.se.sundsvall.digitalmailsender.Details.AccountTypeEnum;
import generated.se.sundsvall.digitalmailsender.Details.PaymentReferenceTypeEnum;
import generated.se.sundsvall.digitalmailsender.DigitalInvoiceRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.SupportInfo;

@Component
class DigitalMailSenderIntegrationMapper {

    DigitalMailRequest toDigitalMailRequest(final DigitalMailDto dto) {
        if (dto == null) {
            return null;
        }

        return new DigitalMailRequest()
            .partyId(dto.partyId())
            .municipalityId(dto.sender().municipalityId())
            .headerSubject(dto.subject())
            .supportInfo(new SupportInfo()
                .supportText(dto.sender().supportInfo().text())
                .contactInformationEmail(dto.sender().supportInfo().emailAddress())
                .contactInformationPhoneNumber(dto.sender().supportInfo().phoneNumber())
                .contactInformationUrl(dto.sender().supportInfo().url()))
            .bodyInformation(new BodyInformation().contentType(dto.contentType().getValue()).body(dto.body()))
            .attachments(ofNullable(dto.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> new Attachment()
                        .contentType(attachment.contentType().getValue())
                        .body(attachment.content())
                        .filename(attachment.filename()))
                    .toList())
                .orElse(null));
    }

    DigitalInvoiceRequest toDigitalInvoiceRequest(final DigitalInvoiceDto dto) {
        if (dto == null) {
            return null;
        }

        return new DigitalInvoiceRequest()
            .partyId(dto.partyId())
            .type(DigitalInvoiceRequest.TypeEnum.fromValue(dto.type().name()))
            .subject(dto.subject())
            .reference(dto.reference())
            .details(new Details()
                .amount(dto.details().amount())
                .dueDate(dto.details().dueDate())
                .paymentReferenceType(PaymentReferenceTypeEnum.fromValue(dto.details().paymentReferenceType().name()))
                .paymentReference(dto.details().paymentReference())
                .accountType(AccountTypeEnum.fromValue(dto.details().accountType().name()))
                .accountNumber(dto.details().accountNumber()))
            .files(dto.files().stream()
                .map(file -> new Attachment()
                    .filename(file.filename())
                    .contentType(file.contentType())
                    .body(file.content()))
                .toList());
    }
}
