package se.sundsvall.messaging.api.model.validation;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

import generated.se.sundsvall.messagingrules.HeaderName;

class ValidHeaderNameConstraintValidator implements ConstraintValidator<ValidHeaderName, HeaderName> {

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
