package se.sundsvall.messaging.integration.party;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
		when(partyClientMock.getLegalIdByPartyId(municipalityId, "PRIVATE", partyId)).thenReturn(ResponseEntity.ok("1234567890"));

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isNotNull().isEqualTo("1234567890");
		verify(partyClientMock).getLegalIdByPartyId(municipalityId, "PRIVATE", partyId);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyIdTest_2() {
		var municipalityId = "2281";
		var partyId = UUID.randomUUID().toString();
		when(partyClientMock.getLegalIdByPartyId(municipalityId, "PRIVATE", partyId)).thenReturn(ResponseEntity.notFound().build());
		when(partyClientMock.getLegalIdByPartyId(municipalityId, "ENTERPRISE", partyId)).thenReturn(ResponseEntity.ok("1234567890"));

		var result = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		assertThat(result).isNotNull().isEqualTo("1234567890");
		verify(partyClientMock).getLegalIdByPartyId(municipalityId, "PRIVATE", partyId);
		verify(partyClientMock).getLegalIdByPartyId(municipalityId, "ENTERPRISE", partyId);
		verifyNoMoreInteractions(partyClientMock);
	}

}
