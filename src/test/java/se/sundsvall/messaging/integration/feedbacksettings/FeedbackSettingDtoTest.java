package se.sundsvall.messaging.integration.feedbacksettings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class FeedbackSettingDtoTest {

    @Test
    void testBuilderAndGetters() {
        String id = UUID.randomUUID().toString();
        String organizationId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();

        FeedbackSettingDto.Channel feedbackChannel = FeedbackSettingDto.Channel.builder()
                .withDestination("john.doe@example.com")
                .withFeedbackWanted(true)
                .withContactMethod(ContactMethod.EMAIL)
                .build();

        FeedbackSettingDto feedbackSetting = FeedbackSettingDto.builder()
                .withId(id)
                .withOrganizationId(organizationId)
                .withPartyId(partyId)
                .withChannels(List.of(feedbackChannel))
                .build();

        assertThat(feedbackSetting.getId()).isEqualTo(id);
        assertThat(feedbackSetting.getOrganizationId()).isEqualTo(organizationId);
        assertThat(feedbackSetting.getPartyId()).isEqualTo(partyId);
        assertThat(feedbackSetting.getChannels()).hasSize(1)
                .allSatisfy(channel -> {
                    assertThat(channel.getDestination()).isEqualTo("john.doe@example.com");
                    assertThat(channel.isFeedbackWanted()).isTrue();
                    assertThat(channel.getContactMethod()).isEqualTo(ContactMethod.EMAIL);
                });

        // For specific coverage ratio, will not meet 85+% without it
        FeedbackSettingDto feedback = feedbackSetting.toBuilder()
                .withId("1")
                .withChannels(List.of(new FeedbackSettingDto.Channel()))
                .build();

        assertThat(feedback.getId()).isEqualTo("1");
        assertThat(feedback.getChannels()).hasSize(1);
    }
}
