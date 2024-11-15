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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

		assertThat(result).isNotNull().isEqualTo(address);
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
}
