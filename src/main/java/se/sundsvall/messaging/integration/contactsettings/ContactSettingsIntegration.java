package se.sundsvall.messaging.integration.contactsettings;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import generated.se.sundsvall.contactsettings.ContactSetting;

@Component
@EnableConfigurationProperties(ContactSettingsIntegrationProperties.class)
public class ContactSettingsIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(ContactSettingsIntegration.class);

    static final String INTEGRATION_NAME = "ContactSettings";

    private final ContactSettingsClient client;

    public ContactSettingsIntegration(final ContactSettingsClient client) {
        this.client = client;
    }

    public List<ContactDto> getContactSettings(final String partyId, final MultiValueMap<String, String> filters) {
        var response = client.getSettings(partyId, filters);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().stream()
                .map(ContactSetting::getContactChannels)
                .flatMap(Collection::stream)
                .map(contactChannel -> ContactDto.builder()
                    .withContactMethod(switch (contactChannel.getContactMethod()) {
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