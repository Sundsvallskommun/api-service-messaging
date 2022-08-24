package se.sundsvall.messaging.integration.businessrules;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.integration.businessrules.domain.DistributionRuleEvaluationDto;

@Component
@EnableConfigurationProperties(BusinessRulesIntegrationProperties.class)
public class BusinessRulesIntegration {

    static final String INTEGRATION_NAME = "BusinessRules";

    private final BusinessRulesClient client;
    private final BusinessRulesIntegrationMapper mapper;

    public BusinessRulesIntegration(final BusinessRulesClient client, final BusinessRulesIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public DistributionRuleEvaluationDto evaluate(final String distributionRuleName) {
        var response = client.evaluate(distributionRuleName);

        if (response.getBody() == null) {
            throw Problem.valueOf(Status.BAD_GATEWAY, "Unable to evaluate");
        }

        return mapper.toDistributionRuleEvaluationDto(response.getBody());
    }
}
