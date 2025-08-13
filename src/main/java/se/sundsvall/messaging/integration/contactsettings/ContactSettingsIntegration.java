package se.sundsvall.messaging.integration.contactsettings;

import static java.util.Collections.emptyList;

import generated.se.sundsvall.contactsettings.ContactSetting;
import java.util.Collection;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
@EnableConfigurationProperties(ContactSettingsIntegrationProperties.class)
public class ContactSettingsIntegration {

	static final String INTEGRATION_NAME = "ContactSettings";

	private final ContactSettingsClient client;

	ContactSettingsIntegration(final ContactSettingsClient client) {
		this.client = client;
	}

	public List<ContactDto> getContactSettings(final String municipalityId, final String partyId, final MultiValueMap<String, String> filters) {
		final var responseList = client.getSettings(municipalityId, partyId, filters).orElse(emptyList());

		return responseList.stream()
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
}
