package se.sundsvall.messaging.integration.businessrules.domain;

import java.util.Map;

import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DistributionRuleEvaluationDto {

    private Map<ContactMethod, Boolean> result;
}
