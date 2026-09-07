package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.messaging.api.model.request.ReferenceableAttachment;
import se.sundsvall.messaging.api.validation.ValidAttachment;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Requires exactly one of {@code content} and {@code objectId}.
 * <p>
 * Both violations are reported against a property rather than against the attachment as a whole, and the missing-both
 * case deliberately names {@code content}. Before the object store reference existed, an attachment without content
 * failed on {@code content} because the field was mandatory; a type-level violation would move that to
 * {@code attachments[i]} and quietly change the shape of the 400 every existing caller already handles.
 */
public class ValidAttachmentConstraintValidator implements ConstraintValidator<ValidAttachment, ReferenceableAttachment> {

	static final String NEITHER_MESSAGE = "either content or objectId must be set";
	static final String BOTH_MESSAGE = "content and objectId are mutually exclusive";

	@Override
	public boolean isValid(final ReferenceableAttachment value, final ConstraintValidatorContext context) {
		if (value == null) {
			return true; // Let @NotNull handle null validation
		}

		final var hasContent = isNotBlank(value.content());
		final var hasObjectId = isNotBlank(value.objectId());

		if (hasContent && hasObjectId) {
			// The reference is what the caller added on top of a request that was already complete without it.
			return violation(context, BOTH_MESSAGE, "objectId");
		}
		if (!hasContent && !hasObjectId) {
			return violation(context, NEITHER_MESSAGE, "content");
		}

		return true;
	}

	private static boolean violation(final ConstraintValidatorContext context, final String message, final String property) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addPropertyNode(property)
			.addConstraintViolation();
		return false;
	}
}
