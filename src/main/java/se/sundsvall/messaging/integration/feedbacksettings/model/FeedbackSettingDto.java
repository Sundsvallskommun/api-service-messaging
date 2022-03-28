package se.sundsvall.messaging.integration.feedbacksettings.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with", toBuilder = true)
@Getter
@NoArgsConstructor
public class FeedbackSettingDto {

    private String id;
    private String organizationId;
    @JsonProperty("personId")
    private String partyId;
    private List<Channel> channels;

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @Builder(setterPrefix = "with", toBuilder = true)
    public static class Channel {

        private ContactMethod contactMethod;
        private String destination;
        @JsonProperty("sendFeedback")
        private boolean feedbackWanted;
    }
}
