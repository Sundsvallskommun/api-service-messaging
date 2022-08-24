package se.sundsvall.messaging.integration.businessrules;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.business-rules")
public class BusinessRulesIntegrationProperties extends AbstractRestIntegrationProperties {

}
