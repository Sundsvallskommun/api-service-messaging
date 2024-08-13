package se.sundsvall.messaging.integration.contactsettings;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import generated.se.sundsvall.contactsettings.ContactSetting;

@FeignClient(
	name = ContactSettingsIntegration.INTEGRATION_NAME,
	url = "${integration.contact-settings.base-url}",
	configuration = ContactSettingsIntegrationConfiguration.class, dismiss404 = true
)
interface ContactSettingsClient {

	@GetMapping("/{municipalityId}/settings")
	ResponseEntity<List<ContactSetting>> getSettings(
		@PathVariable final String municipalityId,
		@RequestParam("partyId") final String partyId,
		@RequestParam("query") final MultiValueMap<String, String> filters);

}
