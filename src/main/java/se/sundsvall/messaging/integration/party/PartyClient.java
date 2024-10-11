package se.sundsvall.messaging.integration.party;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
	name = PartyIntegration.INTEGRATION_NAME,
	url = "${integration.party.base-url}",
	configuration = PartyIntegrationConfiguration.class,
	dismiss404 = true
)
public interface PartyClient {

	@GetMapping("/{municipalityId}/{type}/{partyId}/legalId")
	ResponseEntity<String> getLegalIdByPartyId(
		@PathVariable String municipalityId,
		@PathVariable String type,
		@PathVariable String partyId);

}
