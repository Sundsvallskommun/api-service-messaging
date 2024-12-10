package se.sundsvall.messaging.integration.party;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.party")
public class PartyIntegrationProperties extends AbstractRestIntegrationProperties {

}
