package se.sundsvall.messaging.api.model.request.validation;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

public class OneOfConstraintValidator implements ConstraintValidator<OneOf, String> {

    private List<String> value;

    @Override
    public void initialize(final OneOf annotation) {
        value = Arrays.asList(annotation.value());
    }

    @Override
    public boolean isValid(final String s, final ConstraintValidatorContext context) {
        ((ConstraintValidatorContextImpl) context)
            .addMessageParameter("allowedValues", value);

        if (null == s) {
            return false;
        }

        if (value.isEmpty()) {
            return true;
        }

        return value.contains(s);
    }
}
