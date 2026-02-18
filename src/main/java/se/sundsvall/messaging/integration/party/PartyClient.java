package se.sundsvall.messaging.integration.party;

import generated.se.sundsvall.party.PartyType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static se.sundsvall.messaging.integration.party.PartyIntegration.INTEGRATION_NAME;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.party.base-url}",
	configuration = PartyIntegrationConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = INTEGRATION_NAME)
interface PartyClient {

	@GetMapping("/{municipalityId}/{type}/{partyId}/legalId")
	Optional<String> getLegalIdByPartyId(
		@PathVariable String municipalityId,
		@PathVariable PartyType type,
		@PathVariable String partyId);
}
