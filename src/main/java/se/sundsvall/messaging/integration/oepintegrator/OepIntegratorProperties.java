package se.sundsvall.messaging.integration.oepintegrator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.oep-integrator")
class OepIntegratorProperties extends AbstractRestIntegrationProperties {
}
