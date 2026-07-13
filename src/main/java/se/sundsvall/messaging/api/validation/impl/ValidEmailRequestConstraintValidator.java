package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.validation.ValidEmailRequest;

public class ValidEmailRequestConstraintValidator implements ConstraintValidator<ValidEmailRequest, EmailRequest> {

	@Override
	public boolean isValid(final EmailRequest value, final ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		final var hasEmailAddress = StringUtils.isNotBlank(value.emailAddress());
		final var hasRecipients = Optional.ofNullable(value.recipients()).orElse(List.of()).stream()
			.anyMatch(StringUtils::isNotBlank);

		return hasEmailAddress || hasRecipients;
	}
}
