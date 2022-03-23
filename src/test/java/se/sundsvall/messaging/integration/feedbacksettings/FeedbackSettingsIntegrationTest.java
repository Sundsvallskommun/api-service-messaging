package se.sundsvall.messaging.integration.feedbacksettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class FeedbackSettingsIntegrationTest {

    private static final String PARTY_ID = UUID.randomUUID().toString();

    @Mock
    private RestTemplate restTemplate;
    private FeedbackSettingsIntegration feedbackIntegration;

    @BeforeEach
    void setUp() {
        feedbackIntegration = new FeedbackSettingsIntegration(restTemplate);
    }

    @Test
    void getSettingByPartyId_whenSettingExist_thenReturnsFeedback() {
        var feedback = createFeedbackSetting();
        when(restTemplate.getForObject(anyString(), eq(FeedbackSettingDto[].class))).thenReturn(feedback);

        List<FeedbackSettingDto> feedbackperson = feedbackIntegration.getSettingByPersonId(PARTY_ID);
        List<FeedbackSettingDto> feedbackorg = feedbackIntegration.getSettingByOrganizationId(PARTY_ID);
        assertThat(feedbackperson).isNotEmpty();
        assertThat(feedbackorg).isNotEmpty();
    }

    @Test
    void getSettingByPartyId_whenSettingByPersonIsEmpty_thenReturnsOrganizationSettings() {
        var emptyfeedback = createEmptyFeedbackSetting();
        var feedback = createFeedbackSetting();
        when(restTemplate.getForObject(eq("/settings?personId=" + PARTY_ID), eq(FeedbackSettingDto[].class))).thenReturn(emptyfeedback);
        when(restTemplate.getForObject(eq("/settings?organizationId=" + PARTY_ID), eq(FeedbackSettingDto[].class))).thenReturn(feedback);
        List<FeedbackSettingDto> genericFeedback = feedbackIntegration.getSettingsByPartyId(PARTY_ID);
        assertThat(genericFeedback).isNotEmpty();
    }

    @Test
    void getSettingByPartyId_whenSettingByPersonIsNotEmpty_thenReturnsOrganizationSettings() {
        var feedback = createFeedbackSetting();
        when(restTemplate.getForObject(eq("/settings?personId=" + PARTY_ID), eq(FeedbackSettingDto[].class))).thenReturn(feedback);
        List<FeedbackSettingDto> genericFeedback = feedbackIntegration.getSettingsByPartyId(PARTY_ID);
        assertThat(genericFeedback).isNotEmpty();
    }

    private FeedbackSettingDto[] createFeedbackSetting() {
        var feedback = FeedbackSettingDto.builder()
        .withId(UUID.randomUUID().toString())
        .withPartyId(PARTY_ID)
        .withOrganizationId(UUID.randomUUID().toString())
        .withChannels(Collections.emptyList())
        .build();
        return ArrayUtils.toArray(feedback);
    }

    private FeedbackSettingDto[] createEmptyFeedbackSetting() {
        return ArrayUtils.toArray();
    }
}
