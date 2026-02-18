package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.sundsvall.messaging.api.validation.ValidFolderName;

import static java.util.Objects.isNull;
import static org.springdoc.core.utils.SpringDocUtils.isValidPath;

public class ValidFolderNameValidator implements ConstraintValidator<ValidFolderName, String> {

	private boolean nullable;

	private static final List<Character> INVALID_FOLDER_CHARS = List.of('"', '*', '<', '>', '?', '|', '/', '\\', ':');

	@Override
	public void initialize(final ValidFolderName constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (isNull(value) && nullable) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}

		return INVALID_FOLDER_CHARS.stream()
			.noneMatch(character -> value.contains(character.toString()) // Doesn't contain invalid characters
				|| value.endsWith(" ")    // Doesn't end with space
				|| value.endsWith(".")    // Doesn't end with dot
					&& isValidPath(value));
	}
}
