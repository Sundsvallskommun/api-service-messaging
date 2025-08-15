package se.sundsvall.messaging.integration.contactsettings;

import static se.sundsvall.messaging.integration.contactsettings.ContactSettingsIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.contactsettings.ContactSetting;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.contact-settings.base-url}",
	configuration = ContactSettingsIntegrationConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = INTEGRATION_NAME)
interface ContactSettingsClient {

	@GetMapping("/{municipalityId}/settings")
	Optional<List<ContactSetting>> getSettings(
		@PathVariable final String municipalityId,
		@RequestParam("partyId") final String partyId,
		@RequestParam("query") final MultiValueMap<String, String> filters);
}
