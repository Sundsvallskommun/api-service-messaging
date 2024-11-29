package se.sundsvall.messaging.integration.citizen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.messaging.TestDataFactory.createAddress;
import static se.sundsvall.messaging.integration.citizen.CitizenIntegration.POPULATION_REGISTRATION_ADDRESS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

@ExtendWith(MockitoExtension.class)
class CitizenIntegrationTest {

	@Mock
	private CitizenClient mockCitizenClient;

	@InjectMocks
	private CitizenIntegration citizenIntegration;

	@Test
	void getCitizenAddress() {
		var partyId = UUID.randomUUID().toString();
		var address = createAddress();
		var citizen = new CitizenExtended()
			.givenname(address.firstName())
			.lastname(address.lastName())
			.addresses(List.of(new CitizenAddress()
				.addressType(POPULATION_REGISTRATION_ADDRESS)
				.address(address.address())
				.appartmentNumber(address.apartmentNumber())
				.co(address.careOf())
				.postalCode(address.zipCode())
				.city(address.city())
				.country(address.country())));

		when(mockCitizenClient.getCitizen(partyId)).thenReturn(Optional.of(citizen));

		var result = citizenIntegration.getCitizenAddress(partyId);

		assertThat(result).isNotNull().satisfies(resultAddress -> {
			assertThat(resultAddress.firstName()).isEqualTo(citizenIntegration.capitalize(address.firstName()));
			assertThat(resultAddress.lastName()).isEqualTo(citizenIntegration.capitalize(address.lastName()));
			assertThat(resultAddress.address()).isEqualTo(citizenIntegration.capitalize(address.address()));
			assertThat(resultAddress.apartmentNumber()).isEqualTo(citizenIntegration.capitalize(address.apartmentNumber()));
			assertThat(resultAddress.careOf()).isEqualTo(citizenIntegration.capitalize(address.careOf()));
			assertThat(resultAddress.zipCode()).isEqualTo(citizenIntegration.capitalize(address.zipCode()));
			assertThat(resultAddress.city()).isEqualTo(citizenIntegration.capitalize(address.city()));
			assertThat(resultAddress.country()).isEqualTo(citizenIntegration.capitalize(address.country()));
		});
		verify(mockCitizenClient).getCitizen(partyId);
		verifyNoMoreInteractions(mockCitizenClient);
	}

	@Test
	void getCitizenAddressWhenNoCitizenIsFound() {
		var partyId = UUID.randomUUID().toString();

		when(mockCitizenClient.getCitizen(partyId)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> citizenIntegration.getCitizenAddress(partyId))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
				assertThat(thrownProblem.getTitle()).isEqualTo("No citizen data found");
				assertThat(thrownProblem.getDetail()).isEqualTo("Failed to fetch data from Citizen API");
			});

		verify(mockCitizenClient).getCitizen(partyId);
		verifyNoMoreInteractions(mockCitizenClient);
	}

	@Test
	void getCitizenAddressWhenNoCitizenHasNoPopulationRegistrationAddress() {
		var partyId = UUID.randomUUID().toString();
		var address = createAddress();
		var citizen = new CitizenExtended()
			.givenname(address.firstName())
			.lastname(address.lastName())
			.addresses(List.of(new CitizenAddress()
				.address(address.address())
				.appartmentNumber(address.apartmentNumber())
				.co(address.careOf())
				.postalCode(address.zipCode())
				.city(address.city())
				.country(address.country())));

		when(mockCitizenClient.getCitizen(partyId)).thenReturn(Optional.of(citizen));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> citizenIntegration.getCitizenAddress(partyId))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
				assertThat(thrownProblem.getTitle()).isEqualTo("No citizen address data found");
				assertThat(thrownProblem.getDetail()).isEqualTo("Unable to extract address data from Citizen API");
			});

		verify(mockCitizenClient).getCitizen(partyId);
		verifyNoMoreInteractions(mockCitizenClient);
	}

	@ParameterizedTest
	@ArgumentsSource(CapitalizeArgumentsProvider.class)
	void capitalize(final String input, final String expected) {
		assertThat(citizenIntegration.capitalize(input)).isEqualTo(expected);
	}

	static class CapitalizeArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
			return Stream.of(
				Arguments.of(null, null),
				Arguments.of("", ""),
				Arguments.of("AAA", "Aaa"),
				Arguments.of("bbb", "Bbb"),
				Arguments.of("CCC DDD", "Ccc Ddd"),
				Arguments.of("EEE 123", "Eee 123"),
				Arguments.of("123", "123"));
		}
	}
}
