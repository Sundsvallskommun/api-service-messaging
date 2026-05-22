package se.sundsvall.messaging.api.validation.impl;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.Address;

import static org.assertj.core.api.Assertions.assertThat;

class ValidAddressConstraintValidatorTest {

	private final ValidAddressConstraintValidator validator = new ValidAddressConstraintValidator();

	@Test
	void nullAddressIsValid() {
		assertThat(validator.isValid(null, null)).isTrue();
	}

	@Test
	void onlyFirstNameIsInvalid() {
		var address = Address.builder()
			.withFirstName("John")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}

	@Test
	void onlyLastNameIsInvalid() {
		var address = Address.builder()
			.withLastName("Doe")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}

	@Test
	void firstAndLastNameIsValid() {
		var address = Address.builder()
			.withFirstName("John")
			.withLastName("Doe")
			.build();
		assertThat(validator.isValid(address, null)).isTrue();
	}

	@Test
	void onlyOrganizationNameIsValid() {
		var address = Address.builder()
			.withOrganizationName("Acme AB")
			.build();
		assertThat(validator.isValid(address, null)).isTrue();
	}

	@Test
	void allThreeNamesIsValid() {
		var address = Address.builder()
			.withFirstName("John")
			.withLastName("Doe")
			.withOrganizationName("Acme AB")
			.build();
		assertThat(validator.isValid(address, null)).isTrue();
	}

	@Test
	void noNamesIsInvalid() {
		var address = Address.builder()
			.withAddress("Main Street 1")
			.withZipCode("12345")
			.withCity("Sundsvall")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}

	@Test
	void blankNamesIsInvalid() {
		var address = Address.builder()
			.withFirstName("  ")
			.withLastName("  ")
			.withOrganizationName(" ")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}
}
