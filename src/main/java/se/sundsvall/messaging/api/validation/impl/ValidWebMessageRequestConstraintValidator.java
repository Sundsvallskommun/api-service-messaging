package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.validation.ValidWebMessageRequest;

public class ValidWebMessageRequestConstraintValidator implements ConstraintValidator<ValidWebMessageRequest, WebMessageRequest> {

	@Override
	public boolean isValid(final WebMessageRequest value, final ConstraintValidatorContext context) {
		var partyId = Optional.ofNullable(value.party()).map(WebMessageRequest.Party::partyId).orElse(null);
		var userId = Optional.ofNullable(value.sender()).map(WebMessageRequest.Sender::userId).orElse(null);

		if (value.sendAsOwner()) { // One or the other must be set if sendAsOwner is true
			if (partyId != null && userId != null) {
				context.buildConstraintViolationWithTemplate("Only one of partyId and userId can be set if sendAsOwner is true")
					.addConstraintViolation();
				return false;
			}
			if (partyId == null && userId == null) {
				context.buildConstraintViolationWithTemplate("Both partyId and userId cannot be null if sendAsOwner is true")
					.addConstraintViolation();
				return false;
			}
			return true;
		} else { // Both must be set if sendAsOwner is false
			if (partyId != null && userId != null) {
				return true;
			}
			context.buildConstraintViolationWithTemplate("Both partyId and userId must be set if sendAsOwner is false")
				.addConstraintViolation();
			return false;
		}
	}
}
