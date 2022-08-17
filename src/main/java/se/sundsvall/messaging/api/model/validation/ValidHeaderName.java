package se.sundsvall.messaging.api.model.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidHeaderNameConstraintValidator.class)
public @interface ValidHeaderName {

    String message() default "must be one of: {headerNames}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};
}