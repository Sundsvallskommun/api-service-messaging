package se.sundsvall.messaging.integration.feedbacksettings;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackChannelDto;

import generated.se.sundsvall.feedbacksettings.FeedbackChannel;
import generated.se.sundsvall.feedbacksettings.SearchResult;
import generated.se.sundsvall.feedbacksettings.WeightedFeedbackSetting;

@Component
public class FeedbackSettingsIntegration {

    private final RestTemplate restTemplate;

    public FeedbackSettingsIntegration(@Qualifier("integration.feedback-settings.resttemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FeedbackChannelDto> getSettingsByPartyId(String partyId) {
        var feedbackChannels = searchByPersonId(partyId).stream()
            .flatMap(feedbackSetting -> feedbackSetting.getChannels().stream())
            .toList();

        if (feedbackChannels.isEmpty()) {
            feedbackChannels = searchByOrganizationId(partyId).stream()
                .flatMap(feedbackSetting -> feedbackSetting.getChannels().stream())
                .toList();
        }

        return feedbackChannels.stream()
            .map(this::toDto)
            .toList();
    }

    FeedbackChannelDto toDto(final FeedbackChannel feedbackChannel) {
        return FeedbackChannelDto.builder()
            .withContactMethod(Optional.ofNullable(feedbackChannel.getContactMethod())
                .map(contactMethod -> switch (contactMethod) {
                    case EMAIL -> ContactMethod.EMAIL;
                    case SMS -> ContactMethod.SMS;
                })
                .orElse(ContactMethod.UNKNOWN))
            .withFeedbackWanted(Optional.ofNullable(feedbackChannel.getSendFeedback()).orElse(false))
            .withDestination(feedbackChannel.getDestination())
            .build();
    }

    List<WeightedFeedbackSetting> searchByPersonId(final String partyIdAsPersonId) {
        var url = String.format("/settings?personId=%s", partyIdAsPersonId);

        return Optional.ofNullable(restTemplate.getForObject(url, SearchResult.class))
            .map(SearchResult::getFeedbackSettings)
            .orElse(List.of());
    }

    List<WeightedFeedbackSetting> searchByOrganizationId(final String partyIdAsOrganizationId) {
        String url = String.format("/settings?organizationId=%s", partyIdAsOrganizationId);

        return Optional.ofNullable(restTemplate.getForObject(url, SearchResult.class))
            .map(SearchResult::getFeedbackSettings)
            .orElse(List.of());
    }
}

