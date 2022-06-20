package se.sundsvall.messaging.integration.feedbacksettings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackChannelDto;

import generated.se.sundsvall.feedbacksettings.FeedbackChannel;
import generated.se.sundsvall.feedbacksettings.SearchResult;

@Component
public class FeedbackSettingsIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackSettingsIntegration.class);

    private final RestTemplate restTemplate;

    public FeedbackSettingsIntegration(@Qualifier("integration.feedback-settings.resttemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FeedbackChannelDto> getSettingsByPartyId(String partyId) {
        var feedbackChannels = Optional.ofNullable(searchByPersonId(partyId))
            .stream()
            .map(SearchResult::getFeedbackSettings)
            .flatMap(feedbackSettings -> {
                if (feedbackSettings != null) {
                    return feedbackSettings.stream();
                }

                return Stream.empty();
            })
            // Filter out matches that have organizationId set, to make sure we only process
            // personal feedback settings at this point
            .filter(feedbackSetting -> StringUtils.isBlank(feedbackSetting.getOrganizationId()))
            .flatMap(feedbackSetting -> feedbackSetting.getChannels().stream())
            .toList();

        if (feedbackChannels.isEmpty()) {
            LOG.info("No feedback settings found for personId {}. Checking for matching organizationId", partyId);

            feedbackChannels = Optional.ofNullable(searchByOrganizationId(partyId))
                .stream()
                .map(SearchResult::getFeedbackSettings)
                .flatMap(feedbackSettings -> {
                    if (feedbackSettings != null) {
                        return feedbackSettings.stream();
                    }

                    return Stream.empty();
                })
                .flatMap(feedbackSetting -> feedbackSetting.getChannels().stream())
                .toList();
        }

        if (feedbackChannels.isEmpty()) {
            LOG.info("No feedback settings found for organizationId {}", partyId);
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

    SearchResult searchByPersonId(final String partyIdAsPersonId) {
        var url = String.format("/settings?personId=%s", partyIdAsPersonId);

        return restTemplate.getForObject(url, SearchResult.class);
    }

    SearchResult searchByOrganizationId(final String partyIdAsOrganizationId) {
        String url = String.format("/settings?organizationId=%s", partyIdAsOrganizationId);

        return restTemplate.getForObject(url, SearchResult.class);
    }
}


