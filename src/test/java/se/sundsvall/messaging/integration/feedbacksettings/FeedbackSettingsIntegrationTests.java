package se.sundsvall.messaging.integration.feedbacksettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackSettingDto;

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
        when(mockRestTemplate.getForObject(any(String.class), eq(FeedbackSettingDto[].class)))
            .thenReturn(new FeedbackSettingDto[] { FeedbackSettingDto.builder().build() });

        var feedbackSettings = integration.getSettingsByPartyId("somePartyId");

        assertThat(feedbackSettings).hasSize(1);

        verify(mockRestTemplate, times(1)).getForObject(any(String.class), eq(FeedbackSettingDto[].class));
    }

    @Test
    void test_getSettingsByPartyId_whenNothingIsFoundForPersonId() {
        when(mockRestTemplate.getForObject(any(String.class), eq(FeedbackSettingDto[].class)))
            .thenReturn(new FeedbackSettingDto[] { })
            .thenReturn(new FeedbackSettingDto[] { FeedbackSettingDto.builder().build() });

        var feedbackSettings = integration.getSettingsByPartyId("somePartyId");

        assertThat(feedbackSettings).hasSize(1);

        verify(mockRestTemplate, times(2)).getForObject(any(String.class), eq(FeedbackSettingDto[].class));
    }
}
