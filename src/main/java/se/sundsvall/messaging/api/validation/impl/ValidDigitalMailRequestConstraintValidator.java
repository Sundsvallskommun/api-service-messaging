package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.validation.ValidDigitalMailRequest;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ValidDigitalMailRequestConstraintValidator implements ConstraintValidator<ValidDigitalMailRequest, DigitalMailRequest> {

	@Override
	public boolean isValid(final DigitalMailRequest value, final ConstraintValidatorContext context) {
		if (value == null) {
			return true; // Let @NotNull handle null validation
		}

		// If body is set, contentType must also be set
		if (isNotBlank(value.body()) && !isNotBlank(value.contentType())) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("contentType must be set when body is provided")
				.addPropertyNode("contentType")
				.addConstraintViolation();
			return false;
		}

		return true;
	}
}
