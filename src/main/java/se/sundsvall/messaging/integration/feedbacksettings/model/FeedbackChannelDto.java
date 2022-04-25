package se.sundsvall.messaging.integration.feedbacksettings.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with", toBuilder = true)
public class FeedbackChannelDto {

    private ContactMethod contactMethod;
    private String destination;
    private boolean feedbackWanted;
}
