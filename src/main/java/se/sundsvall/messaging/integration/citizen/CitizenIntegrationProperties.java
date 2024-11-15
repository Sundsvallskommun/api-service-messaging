package se.sundsvall.messaging.integration.citizen;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.citizen")
class CitizenIntegrationProperties extends AbstractRestIntegrationProperties {

}
