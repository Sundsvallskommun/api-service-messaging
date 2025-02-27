package se.sundsvall.messaging.integration.citizen;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.messaging.integration.citizen.CitizenIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.citizen.CitizenExtended;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
interface CitizenClient {

	@GetMapping(path = "/{municipalityId}/{personId}?ShowClassified=false", produces = APPLICATION_JSON_VALUE)
	Optional<CitizenExtended> getCitizen(
		@PathVariable(name = "municipalityId") String municipalityId,
		@PathVariable(name = "personId") String personId);
}
