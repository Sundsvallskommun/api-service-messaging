package se.sundsvall.messaging.integration.businessrules;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.integration.businessrules.domain.DistributionRuleEvaluationDto;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;

import generated.se.sundsvall.businessrules.DistributionRuleEvaluationResponse;

@Component
class BusinessRulesIntegrationMapper {

    DistributionRuleEvaluationDto toDistributionRuleEvaluationDto(final DistributionRuleEvaluationResponse response) {
        var result = response.entrySet().stream()
            .collect(toMap(entry -> ContactMethod.valueOf(entry.getKey()), Map.Entry::getValue));

        return DistributionRuleEvaluationDto.builder()
            .withResult(result)
            .build();
    }
}
