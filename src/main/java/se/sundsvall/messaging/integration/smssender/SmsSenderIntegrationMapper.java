package se.sundsvall.messaging.integration.smssender;

import static java.util.Optional.ofNullable;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsRequest.PriorityEnum;
import generated.se.sundsvall.smssender.Sender;
import se.sundsvall.messaging.api.model.request.Priority;

@Component
class SmsSenderIntegrationMapper {

	SendSmsRequest toSendSmsRequest(final SmsDto dto) {
		if (dto == null) {
			return null;
		}

		return new SendSmsRequest()
			.sender(new Sender().name(dto.sender()))
			.mobileNumber(dto.mobileNumber())
			.message(dto.message())
			.priority(toPriorityEnum(dto.priority()));
	}

	private PriorityEnum toPriorityEnum(Priority priority) {
		return ofNullable(priority).map(p -> (switch (p) {
			case Priority.HIGH -> PriorityEnum.HIGH;
			default -> PriorityEnum.NORMAL;
		})).orElse(PriorityEnum.NORMAL);
	}
}
