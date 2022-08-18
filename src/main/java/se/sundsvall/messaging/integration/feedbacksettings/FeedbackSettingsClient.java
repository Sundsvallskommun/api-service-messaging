package se.sundsvall.messaging.integration.feedbacksettings;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import generated.se.sundsvall.feedbacksettings.SearchResult;

@FeignClient(
    name = FeedbackSettingsIntegration.INTEGRATION_NAME,
    url = "${integration.feedback-settings.base-url}",
    configuration = FeedbackSettingsIntegrationConfiguration.class
)
public interface FeedbackSettingsClient {

    @GetMapping("/settings")
    SearchResult searchByPersonId(@RequestHeader final HttpHeaders headers,
            @RequestParam("personId") final String partyIdAsPersonId);

    @GetMapping("/settings")
    SearchResult searchByOrganizationId(@RequestHeader final HttpHeaders headers,
            @RequestParam("organizationId") final String partyIdAsOrganizationId);
}