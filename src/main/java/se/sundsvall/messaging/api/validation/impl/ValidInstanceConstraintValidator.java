package se.sundsvall.messaging.api.validation.impl;

import static java.util.Objects.isNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.messaging.api.validation.ValidInstance;

public class ValidInstanceConstraintValidator implements ConstraintValidator<ValidInstance, String> {

	private boolean nullable;

	@Override
	public void initialize(final ValidInstance constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	public boolean isValid(final String value) {
		return isValid(value, null);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (isNull(value)) {
			return nullable;
		}
		return "internal".equalsIgnoreCase(value) || "external".equalsIgnoreCase(value);
	}

}
