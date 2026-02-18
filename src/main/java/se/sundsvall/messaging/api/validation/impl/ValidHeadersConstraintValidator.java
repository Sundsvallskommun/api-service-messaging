package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import se.sundsvall.messaging.api.model.request.Header;
import se.sundsvall.messaging.api.validation.ValidHeaders;

import static org.apache.commons.collections4.MapUtils.isEmpty;

public class ValidHeadersConstraintValidator implements ConstraintValidator<ValidHeaders, Map<?, ?>> {

	private final Set<String> allowedHeaderNames;

	public ValidHeadersConstraintValidator() {
		allowedHeaderNames = Arrays.stream(Header.values())
			.map(header -> String.format("%s/%s", header.getKey(), header.name()))
			.collect(Collectors.toSet());
	}

	@Override
	public boolean isValid(final Map<?, ?> values, final ConstraintValidatorContext context) {
		if (isEmpty(values)) {
			return true;
		}

		var unwrappedContext = context.unwrap(HibernateConstraintValidatorContext.class);
		unwrappedContext.disableDefaultConstraintViolation();

		var isValid = true;
		for (var entry : values.entrySet()) {
			var headerName = entry.getKey().toString();
			var headerValues = ((List<?>) entry.getValue()).stream()
				.map(Object::toString)
				.toList();

			var matchingHeader = Header.fromString(headerName);
			if (matchingHeader == null) {
				unwrappedContext
					.buildConstraintViolationWithTemplate(headerName + " is not a valid header. Allowed headers are: " + allowedHeaderNames)
					.addPropertyNode(headerName)
					.addConstraintViolation();
				isValid = false;
				continue;
			}

			var pattern = matchingHeader.getPattern();
			var message = matchingHeader.getMessage();
			for (var headerValue : headerValues) {
				if (!pattern.matcher(headerValue).matches()) {
					unwrappedContext
						.buildConstraintViolationWithTemplate(message)
						.addPropertyNode(headerName)
						.addConstraintViolation();
					isValid = false;
				}
			}
		}

		return isValid;
	}
}
