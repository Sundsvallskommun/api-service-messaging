package se.sundsvall.messaging.integration.feedbacksettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;

import generated.se.sundsvall.feedbacksettings.FeedbackChannel;
import generated.se.sundsvall.feedbacksettings.SearchResult;
import generated.se.sundsvall.feedbacksettings.WeightedFeedbackSetting;

@ExtendWith(MockitoExtension.class)
class FeedbackSettingsIntegrationTests {

    @Mock
    private RestTemplate mockRestTemplate;

    private FeedbackSettingsIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new FeedbackSettingsIntegration(mockRestTemplate);
    }

    @Test
    void test_getSettingsByPartyId() {
        var setting = new WeightedFeedbackSetting()
            .id("someId")
            .channels(List.of(new FeedbackChannel()));

        when(mockRestTemplate.getForObject(any(String.class), eq(SearchResult.class)))
            .thenReturn(new SearchResult().feedbackSettings(List.of(setting)));

        var feedbackChannels = integration.getSettingsByPartyId("somePartyId");

        assertThat(feedbackChannels).hasSize(1);

        verify(mockRestTemplate, times(1)).getForObject(any(String.class), eq(SearchResult.class));
    }

    @Test
    void test_getSettingsByPartyId_whenNothingIsFoundForPersonId() {
        var setting = new WeightedFeedbackSetting()
            .id("someId")
            .channels(List.of(new FeedbackChannel()));

        when(mockRestTemplate.getForObject(any(String.class), eq(SearchResult.class)))
            .thenReturn(new SearchResult())
            .thenReturn(new SearchResult().feedbackSettings(List.of(setting)));

        var feedbackSettings = integration.getSettingsByPartyId("somePartyId");

        assertThat(feedbackSettings).hasSize(1);

        verify(mockRestTemplate, times(2)).getForObject(any(String.class), eq(SearchResult.class));
    }

    @Test
    void test_toDto_withEmailAsContactMethod() {
        var feedbackChannel = new FeedbackChannel()
            .sendFeedback(true)
            .contactMethod(FeedbackChannel.ContactMethodEnum.EMAIL)
            .destination("someDestination");

        var dto = integration.toDto(feedbackChannel);

        assertThat(dto.isFeedbackWanted()).isTrue();
        assertThat(dto.getContactMethod()).isEqualTo(ContactMethod.EMAIL);
        assertThat(dto.getDestination()).isEqualTo("someDestination");
    }

    @Test
    void test_toDto_withSmsAsContactMethod() {
        var feedbackChannel = new FeedbackChannel()
            .sendFeedback(true)
            .contactMethod(FeedbackChannel.ContactMethodEnum.SMS)
            .destination("someDestination");

        var dto = integration.toDto(feedbackChannel);

        assertThat(dto.isFeedbackWanted()).isTrue();
        assertThat(dto.getContactMethod()).isEqualTo(ContactMethod.SMS);
        assertThat(dto.getDestination()).isEqualTo("someDestination");
    }

    @Test
    void test_toDto_withNullContactMethod() {
        var feedbackChannel = new FeedbackChannel()
            .sendFeedback(true)
            .destination("someDestination");

        var dto = integration.toDto(feedbackChannel);

        assertThat(dto.isFeedbackWanted()).isTrue();
        assertThat(dto.getContactMethod()).isEqualTo(ContactMethod.UNKNOWN);
        assertThat(dto.getDestination()).isEqualTo("someDestination");
    }
}
