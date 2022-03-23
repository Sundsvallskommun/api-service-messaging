package se.sundsvall.messaging.integration.feedbacksettings;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FeedbackSettingsIntegration {

    private final RestTemplate restTemplate;

    public FeedbackSettingsIntegration(@Qualifier("integration.feedback-settings.resttemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FeedbackSettingDto> getSettingsByPartyId(String partyId) {
        var feedback = getSettingByPersonId(partyId);

        if (feedback.isEmpty()) {
            return getSettingByOrganizationId(partyId);
        }   
        return feedback;
    }

    List<FeedbackSettingDto> getSettingByPersonId(String partyId) {
        String url = String.format("/settings?personId=%s", partyId);
        FeedbackSettingDto[] feedback = restTemplate.getForObject(url, FeedbackSettingDto[].class);
        return List.of(feedback);
    }

    List<FeedbackSettingDto> getSettingByOrganizationId(String partyId) {
        String url = String.format("/settings?organizationId=%s", partyId);
        FeedbackSettingDto[] feedback = restTemplate.getForObject(url, FeedbackSettingDto[].class);
        return List.of(feedback);
    }
}

