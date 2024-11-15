package se.sundsvall.messaging.integration.citizen;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.messaging.integration.citizen.CitizenIntegration.INTEGRATION_NAME;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.citizen.CitizenExtended;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
interface CitizenClient {

	@GetMapping(path = "/{personId}?ShowClassified=false", produces = TEXT_PLAIN_VALUE, consumes = APPLICATION_JSON_VALUE)
	Optional<CitizenExtended> getCitizen(@PathVariable(name = "personId") String personId);
}
