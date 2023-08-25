package se.sundsvall.messaging.integration.contactsettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.contactsettings.ContactChannel;
import generated.se.sundsvall.contactsettings.ContactMethod;
import generated.se.sundsvall.contactsettings.ContactSetting;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ContactSettingsIntegrationTests {

    @Mock
    private ContactSettingsClient mockClient;

    @InjectMocks
    private ContactSettingsIntegration integration;

    @Test
    void test_getSettings() {
        var contactSetting = new ContactSetting()
            .contactChannels(List.of(new ContactChannel()
                .contactMethod(ContactMethod.EMAIL)
                .destination("someone@something.com")
                .disabled(false)
            ));

        when(mockClient.getSettings(any(String.class), any()))
            .thenReturn(ResponseEntity.ok(List.of(contactSetting)));

        var contactDtos = integration.getContactSettings("somePartyId", new LinkedMultiValueMap<>());

        assertThat(contactDtos).hasSize(1);
        assertThat(contactDtos.get(0).contactMethod()).isEqualTo(ContactDto.ContactMethod.EMAIL);
        assertThat(contactDtos.get(0).destination()).isEqualTo("someone@something.com");
        assertThat(contactDtos.get(0).disabled()).isFalse();

        verify(mockClient, times(1)).getSettings(any(String.class), any());
    }

    @Test
    void test_getSettings_whenSettingsAreNotFound() {
        when(mockClient.getSettings(any(String.class), any()))
            .thenReturn(ResponseEntity.notFound().build());

        var contactDtos = integration.getContactSettings("somePartyId", new LinkedMultiValueMap<>());

        assertThat(contactDtos).isEmpty();

        verify(mockClient, times(1)).getSettings(any(String.class), any());
    }

    @Test
    void test_getSettings_whenSettingsContainNoChannels() {
        var contactSetting = new ContactSetting()
            .contactChannels(List.of());

        when(mockClient.getSettings(any(String.class), any()))
            .thenReturn(ResponseEntity.ok(List.of(contactSetting)));

        var contactDtos = integration.getContactSettings("somePartyId", new LinkedMultiValueMap<>());

        assertThat(contactDtos).isEmpty();

        verify(mockClient, times(1)).getSettings(any(String.class), any());
    }
}
