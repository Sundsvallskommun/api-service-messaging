package se.sundsvall.messaging.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.messaging.api.validation.impl.ValidEmailRequestConstraintValidator;

/**
 * Annotation for validating that an email request has at least one recipient, provided either through the deprecated
 * single 'emailAddress' attribute or the 'recipients' list.
 */
@Documented
@Target({
	ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEmailRequestConstraintValidator.class)
public @interface ValidEmailRequest {

	String message() default "at least one recipient must be provided in 'emailAddress' or 'recipients'";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
