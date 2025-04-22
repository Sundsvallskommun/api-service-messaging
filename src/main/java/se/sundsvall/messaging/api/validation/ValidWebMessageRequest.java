package se.sundsvall.messaging.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.messaging.api.validation.impl.ValidWebMessageRequestConstraintValidator;

/**
 * Annotation for validating that a web message sent as an owner has a partyId and a web message sent as an
 * administrator has a userId.
 */
@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidWebMessageRequestConstraintValidator.class)
public @interface ValidWebMessageRequest {

	String message() default "Invalid web message request";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
