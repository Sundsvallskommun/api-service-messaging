package se.sundsvall.messaging.integration.contactsettings;

import java.util.Collection;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import generated.se.sundsvall.contactsettings.ContactSetting;

@Component
@EnableConfigurationProperties(ContactSettingsIntegrationProperties.class)
public class ContactSettingsIntegration {

	static final String INTEGRATION_NAME = "ContactSettings";

	private final ContactSettingsClient client;

	ContactSettingsIntegration(final ContactSettingsClient client) {
		this.client = client;
	}

	public List<ContactDto> getContactSettings(final String municipalityId, final String partyId, final MultiValueMap<String, String> filters) {
		final var response = client.getSettings(municipalityId, partyId, filters);

		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return response.getBody().stream()
				.map(ContactSetting::getContactChannels)
				.flatMap(Collection::stream)
				.map(contactChannel -> ContactDto.builder()
					.withContactMethod(switch (contactChannel.getContactMethod())
					{
						case SMS -> ContactDto.ContactMethod.SMS;
						case EMAIL -> ContactDto.ContactMethod.EMAIL;
					})
					.withDestination(contactChannel.getDestination())
					.withDisabled(contactChannel.getDisabled())
					.build())
				.toList();
		}

		return List.of();
	}

}
