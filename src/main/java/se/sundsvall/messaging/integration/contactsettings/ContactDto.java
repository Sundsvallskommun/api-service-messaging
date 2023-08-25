package se.sundsvall.messaging.integration.contactsettings;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record ContactDto(ContactMethod contactMethod, String destination, boolean disabled) {

    public enum ContactMethod {

        EMAIL, SMS, NO_CONTACT, UNKNOWN
    }
}
