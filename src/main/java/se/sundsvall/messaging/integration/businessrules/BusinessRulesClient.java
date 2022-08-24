package se.sundsvall.messaging.integration.businessrules;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import generated.se.sundsvall.businessrules.DistributionRuleEvaluationResponse;

@FeignClient(
    name = BusinessRulesIntegration.INTEGRATION_NAME,
    url = "${integration.business-rules.base-url}",
    configuration = BusinessRulesIntegrationConfiguration.class
)
public interface BusinessRulesClient {

    @PostMapping("/distributionRule/{distributionRuleName}")
    ResponseEntity<DistributionRuleEvaluationResponse> evaluate(
        @PathVariable("distributionRuleName") final String distributionRuleName);
}
