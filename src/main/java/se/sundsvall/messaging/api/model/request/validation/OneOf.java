package se.sundsvall.messaging.api.model.request.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneOfConstraintValidator.class)
public @interface OneOf {

    String message() default "must be one of: {allowedValues}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};

    String[] value();
}
