package se.sundsvall.messaging.api.model.request.validation;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

import generated.se.sundsvall.messagingrules.HeaderName;

public class ValidHeaderNameConstraintValidator implements ConstraintValidator<ValidHeaderName, HeaderName> {

    private final String headerNames;

    public ValidHeaderNameConstraintValidator() {
        headerNames = Arrays.stream(HeaderName.values()).map(HeaderName::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean isValid(final HeaderName headerName, final ConstraintValidatorContext context) {
        boolean valid = headerName != null;
        if (!valid) {
            ((ConstraintValidatorContextImpl) context).addMessageParameter("headerNames", headerNames);
        }

        return valid;
    }
}
