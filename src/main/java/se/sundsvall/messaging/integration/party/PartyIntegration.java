package se.sundsvall.messaging.integration.party;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.party.PartyType;

@Component
@EnableConfigurationProperties(PartyIntegrationProperties.class)
public class PartyIntegration {

	static final String INTEGRATION_NAME = "Party";

	private final PartyClient client;

	public PartyIntegration(final PartyClient client) {
		this.client = client;
	}

	public String getLegalIdByPartyId(final String municipalityId, final String partyId) {
		return client.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)
			.orElseGet(() -> client.getLegalIdByPartyId(municipalityId, PartyType.ENTERPRISE, partyId)
				.orElse(null));
	}

}
