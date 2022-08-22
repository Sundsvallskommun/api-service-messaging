package se.sundsvall.messaging.integration.feedbacksettings;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackChannelDto;
import se.sundsvall.messaging.model.Header;

import generated.se.sundsvall.feedbacksettings.FeedbackChannel;
import generated.se.sundsvall.feedbacksettings.SearchResult;
import generated.se.sundsvall.messagingrules.HeaderName;

@Component
@EnableConfigurationProperties(FeedbackSettingsIntegrationProperties.class)
public class FeedbackSettingsIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackSettingsIntegration.class);

    static final String INTEGRATION_NAME = "FeedbackSettings";

    static final String FILTER_HEADER_PREFIX = "x-filter-";

    private final FeedbackSettingsClient client;

    public FeedbackSettingsIntegration(final FeedbackSettingsClient client) {
        this.client = client;
    }

    public List<FeedbackChannelDto> getSettingsByPartyId(final List<Header> headers, final String partyId) {
        var httpHeaders = toHttpHeaders(headers, Set.of(HeaderName.DISTRIBUTION_RULE));

        var feedbackChannels = Optional.ofNullable(client.searchByPersonId(httpHeaders, partyId))
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

            feedbackChannels = Optional.ofNullable(client.searchByOrganizationId(httpHeaders, partyId))
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

    HttpHeaders toHttpHeaders(final List<Header> headers, final Set<HeaderName> skipHeaders) {
        var httpHeaders = new HttpHeaders();
        headers.stream()
            .filter(header -> skipHeaders.isEmpty() || !skipHeaders.contains(header.getName()))
            .forEach(header -> httpHeaders.addAll(getHeaderFilterName(header), header.getValues()));
        return httpHeaders;
    }

    String getHeaderFilterName(final Header header) {
        return FILTER_HEADER_PREFIX + header.getName().toString().toLowerCase().replace('_', '-');
    }
}