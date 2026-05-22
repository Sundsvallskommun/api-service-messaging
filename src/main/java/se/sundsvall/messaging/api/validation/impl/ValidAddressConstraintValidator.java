package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.messaging.api.validation.ValidAddress;
import se.sundsvall.messaging.model.Address;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ValidAddressConstraintValidator implements ConstraintValidator<ValidAddress, Address> {

	@Override
	public boolean isValid(final Address value, final ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		final var hasPersonName = isNotBlank(value.firstName()) && isNotBlank(value.lastName());
		final var hasOrganizationName = isNotBlank(value.organizationName());
		return hasPersonName || hasOrganizationName;
	}
}
