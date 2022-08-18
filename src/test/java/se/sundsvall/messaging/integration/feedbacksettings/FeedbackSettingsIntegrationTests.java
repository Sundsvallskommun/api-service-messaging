package se.sundsvall.messaging.integration.feedbacksettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.model.Header;

import generated.se.sundsvall.feedbacksettings.FeedbackChannel;
import generated.se.sundsvall.feedbacksettings.SearchResult;
import generated.se.sundsvall.feedbacksettings.WeightedFeedbackSetting;
import generated.se.sundsvall.messagingrules.HeaderName;

@ExtendWith(MockitoExtension.class)
class FeedbackSettingsIntegrationTests {

    @Mock
    private FeedbackSettingsClient mockClient;

    private FeedbackSettingsIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new FeedbackSettingsIntegration(mockClient);
    }

    @Test
    void test_getSettingsByPartyId() {
        var setting = new WeightedFeedbackSetting()
            .id("someId")
            .channels(List.of(new FeedbackChannel()));

        when(mockClient.searchByPersonId(any(HttpHeaders.class), any(String.class)))
            .thenReturn(new SearchResult().feedbackSettings(List.of(setting)));

        var feedbackChannels = integration.getSettingsByPartyId(List.of(), "somePartyId");

        assertThat(feedbackChannels).hasSize(1);

        verify(mockClient, times(1)).searchByPersonId(any(HttpHeaders.class), any(String.class));
    }

    @Test
    void test_getSettingsByPartyId_whenNothingIsFoundForPersonId() {
        var setting = new WeightedFeedbackSetting()
            .id("someId")
            .channels(List.of(new FeedbackChannel()));

        when(mockClient.searchByPersonId(any(HttpHeaders.class), any(String.class)))
            .thenReturn(new SearchResult());
        when(mockClient.searchByOrganizationId(any(HttpHeaders.class), any(String.class)))
            .thenReturn(new SearchResult().feedbackSettings(List.of(setting)));

        var feedbackSettings = integration.getSettingsByPartyId(List.of(), "somePartyId");

        assertThat(feedbackSettings).hasSize(1);

        verify(mockClient, times(1)).searchByPersonId(any(HttpHeaders.class), any(String.class));
        verify(mockClient, times(1)).searchByOrganizationId(any(HttpHeaders.class), any(String.class));
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

    @Test
    void test_toHttpHeaders() {
        var headers = List.of(
            Header.builder()
                .withName(HeaderName.DISTRIBUTION_RULE)
                .withValues(List.of("someValue"))
                .build(),
            Header.builder()
                .withName(HeaderName.FACILITY_ID)
                .withValues(List.of("someValue"))
                .build());

        var httpHeaders = integration.toHttpHeaders(headers, Set.of());

        assertThat(httpHeaders).hasSize(2);
        assertThat(httpHeaders.keySet()).allSatisfy(headerName -> {
            assertThat(headerName).startsWith(FeedbackSettingsIntegration.FILTER_HEADER_PREFIX);
            assertThat(headerName).isLowerCase();
            assertThat(headerName).doesNotContain("_").contains("-");
        });
    }

    @Test
    void test_toHttpHeaders_withSkipHeaders() {
        var headers = List.of(
            Header.builder()
                .withName(HeaderName.DISTRIBUTION_RULE)
                .withValues(List.of("someValue"))
                .build(),
            Header.builder()
                .withName(HeaderName.FACILITY_ID)
                .withValues(List.of("someValue"))
                .build());

        var httpHeaders = integration.toHttpHeaders(headers, Set.of(HeaderName.DISTRIBUTION_RULE));

        assertThat(httpHeaders).hasSize(1);
    }

    @Test
    void test_getHeaderFilterName() {
        var header = Header.builder()
            .withName(HeaderName.FACILITY_ID)
            .withValues(List.of("someValue"))
            .build();

        var headerFilterName = integration.getHeaderFilterName(header);

        assertThat(headerFilterName).startsWith(FeedbackSettingsIntegration.FILTER_HEADER_PREFIX);
        assertThat(headerFilterName).isLowerCase();
        assertThat(headerFilterName).doesNotContain("_").contains("-");
    }
}
