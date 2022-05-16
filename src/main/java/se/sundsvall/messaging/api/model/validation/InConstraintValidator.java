package se.sundsvall.messaging.api.model.validation;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

class InConstraintValidator implements ConstraintValidator<In, String> {

    private List<String> value;

    @Override
    public void initialize(final In inAnnotation) {
        value = Arrays.asList(inAnnotation.value());
    }

    @Override
    public boolean isValid(final String s, final ConstraintValidatorContext context) {
        if (null == s) {
            return false;
        }

        boolean valid = value.contains(s);
        if (!valid) {
            ((ConstraintValidatorContextImpl) context)
                .addMessageParameter("allowedValues", value);
        }

        return valid;
    }
}
