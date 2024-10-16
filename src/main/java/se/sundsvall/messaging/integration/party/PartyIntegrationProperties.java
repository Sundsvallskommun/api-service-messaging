package se.sundsvall.messaging.integration.party;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.party")
public class PartyIntegrationProperties extends AbstractRestIntegrationProperties {

}
