package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.messaging.api.validation.ValidNullOrNotEmpty;

import static java.util.Objects.isNull;

/**
 * Validator for {@link ValidNullOrNotEmpty} annotation.
 * Checks if a string is null or not empty and has no starting or trailing spaces.
 * If it fails, the value is not valid.
 */
public class ValidNullOrNotEmptyValidator implements ConstraintValidator<ValidNullOrNotEmpty, String> {

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (isNull(value)) {
			return true;
		}
		return isValidFormat(value);
	}

	public boolean isValidFormat(final String value) {
		return !value.isEmpty() && !value.startsWith(" ") && !value.endsWith(" ");
	}
}
