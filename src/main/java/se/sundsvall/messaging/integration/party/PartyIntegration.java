package se.sundsvall.messaging.integration.party;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(PartyIntegrationProperties.class)
public class PartyIntegration {

	static final String INTEGRATION_NAME = "Party";

	private final PartyClient client;

	public PartyIntegration(final PartyClient client) {
		this.client = client;
	}

	public String getLegalIdByPartyId(final String municipalityId, final String partyId) {
		var response = client.getLegalIdByPartyId(municipalityId, "PRIVATE", partyId);
		if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
			response = client.getLegalIdByPartyId(municipalityId, "ENTERPRISE", partyId);
		}
		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		}
		return null;
	}

}
