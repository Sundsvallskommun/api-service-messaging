package se.sundsvall.messaging.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidFolderNameValidatorTest {

	private final ValidFolderNameValidator validator = new ValidFolderNameValidator();

	@ParameterizedTest
	@ValueSource(strings = {
		"fol\"der", "fol*der", "fol<der", "fol>der", "fol?der", "fol|der", "fol/der", "fol\\der", "fol:der"
	})
	void testInvalidCharacters(String folderName) {
		assertThat(validator.isValid(folderName, null)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"folder ", "folder."
	})
	void testFolderEndsWithInvalidCharacter(String folderName) {
		assertThat(validator.isValid(folderName, null)).isFalse();
	}

	@Test
	void testNullFolderName() {
		assertThat(validator.isValid(null, null)).isFalse();
	}

	@Test
	void testValidFolder() {
		assertThat(validator.isValid("validfolder", null)).isTrue();
	}
}
