package se.sundsvall.messaging.integration.snailmailsender;

import generated.se.sundsvall.snailmail.Attachment;
import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.request.Address;

@Component
class SnailMailSenderIntegrationMapper {

	SendSnailMailRequest toSendSnailmailRequest(final SnailMailDto dto) {
		if (dto == null) {
			return null;
		}

		return new SendSnailMailRequest()
			.partyId(dto.partyId())
			.address(toAddress(dto.address()))
			.batchId(dto.batchId())
			.department(dto.department())
			.deviation(dto.deviation())
			.attachments(Optional.ofNullable(dto.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> new Attachment()
						.name(attachment.name())
						.contentType(Attachment.ContentTypeEnum.fromValue(attachment.contentType()))
						.content(attachment.content()))
					.toList())
				.orElse(null));
	}

	generated.se.sundsvall.snailmail.Address toAddress(final Address address) {
		return Optional.ofNullable(address).map(notNull -> new generated.se.sundsvall.snailmail.Address()
			.firstName(address.firstName())
			.lastName(address.lastName())
			.city(address.city())
			.apartmentNumber(address.apartmentNumber())
			.organizationNumber(address.organizationNumber())
			.address(address.address())
			.careOf(address.careOf())
			.zipCode(address.zipCode())
			.country(address.country()))
			.orElse(null);
	}

}
