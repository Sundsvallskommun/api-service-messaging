package se.sundsvall.messaging.integration.citizen;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.messaging.model.Address;

@Component
@EnableConfigurationProperties(CitizenIntegrationProperties.class)
public class CitizenIntegration {

	static final String INTEGRATION_NAME = "Citizen";

	static final String POPULATION_REGISTRATION_ADDRESS = "POPULATION_REGISTRATION_ADDRESS";

	private final CitizenClient client;

	CitizenIntegration(final CitizenClient client) {
		this.client = client;
	}

	public Address getCitizenAddress(final String partyId, final String municipalityId) {
		final var citizen = client.getCitizen(municipalityId, partyId)
			.orElseThrow(() -> Problem.builder()
				.withTitle("No citizen data found")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Failed to fetch data from Citizen API")
				.build());

		return ofNullable(citizen.getAddresses())
			.orElse(emptyList())
			.stream()
			.filter(address -> POPULATION_REGISTRATION_ADDRESS.equals(address.getAddressType()))
			.findFirst()
			.map(address -> Address.builder()
				.withFirstName(capitalize(citizen.getGivenname()))
				.withLastName(capitalize(citizen.getLastname()))
				.withAddress(capitalize(address.getAddress()))
				.withApartmentNumber(capitalize(address.getAppartmentNumber()))
				.withCareOf(capitalize(address.getCo()))
				.withZipCode(capitalize(address.getPostalCode()))
				.withCity(capitalize(address.getCity()))
				.withCountry(capitalize(address.getCountry()))
				.build())
			.orElseThrow(() -> Problem.builder()
				.withTitle("No citizen address data found")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Unable to extract address data from Citizen API")
				.build());
	}

	String capitalize(final String string) {
		if (isNull(string)) {
			return null;
		}

		return Arrays.stream(string.split("\\s+"))
			.map(String::toLowerCase)
			.map(StringUtils::capitalize)
			.collect(joining(" "));
	}
}
