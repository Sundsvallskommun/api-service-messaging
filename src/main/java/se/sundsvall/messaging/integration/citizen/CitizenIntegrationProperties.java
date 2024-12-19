package se.sundsvall.messaging.integration.citizen;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.citizen")
class CitizenIntegrationProperties extends AbstractRestIntegrationProperties {

}
