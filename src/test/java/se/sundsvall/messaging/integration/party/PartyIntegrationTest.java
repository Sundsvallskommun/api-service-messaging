package se.sundsvall.messaging.integration.party;

import generated.se.sundsvall.party.PartyType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	@Mock
	private PartyClient partyClientMock;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@Test
	void getLegalIdByPartyIdTest_1() {
		var municipalityId = "2281";
		var partyId = UUID.randomUUID().toString();
		when(partyClientMock.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)).thenReturn(Optional.of("1234567890"));

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isNotNull().isEqualTo("1234567890");
		verify(partyClientMock).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyIdTest_2() {
		var municipalityId = "2281";
		var partyId = UUID.randomUUID().toString();
		when(partyClientMock.getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId)).thenReturn(Optional.empty());
		when(partyClientMock.getLegalIdByPartyId(municipalityId, PartyType.ENTERPRISE, partyId)).thenReturn(Optional.of("1234567890"));

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isNotNull().isEqualTo("1234567890");
		verify(partyClientMock).getLegalIdByPartyId(municipalityId, PartyType.PRIVATE, partyId);
		verify(partyClientMock).getLegalIdByPartyId(municipalityId, PartyType.ENTERPRISE, partyId);
		verifyNoMoreInteractions(partyClientMock);
	}

}
