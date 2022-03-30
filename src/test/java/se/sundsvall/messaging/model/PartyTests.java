package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createParty;

import org.junit.jupiter.api.Test;

class PartyTests {

    @Test
    void testBuilderAndGetters() {
        var party = createParty(p -> p.setPartyId("somePartyId"));

        assertThat(party.getPartyId()).isEqualTo("somePartyId");
        assertThat(party.getExternalReferences()).hasSize(1);
    }
}
