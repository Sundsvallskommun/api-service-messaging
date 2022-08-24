package se.sundsvall.messaging.integration.businessrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import generated.se.sundsvall.businessrules.DistributionRuleEvaluationResponse;

class BusinessRulesIntegrationMapperTests {

    private final BusinessRulesIntegrationMapper mapper = new BusinessRulesIntegrationMapper();

    @Test
    void test_toDistributionRuleEvaluationDto() {
        var response = new DistributionRuleEvaluationResponse();
        response.put("SMS", true);
        response.put("EMAIL", false);

        var dto = mapper.toDistributionRuleEvaluationDto(response);
        assertThat(dto.getResult()).hasSameSizeAs(response);
    }

    @Test
    void test_toDistributionRuleEvaluationDto_whenResponseIsEmpty() {
        var dto = mapper.toDistributionRuleEvaluationDto(new DistributionRuleEvaluationResponse());
        assertThat(dto.getResult()).isEmpty();
    }
}
