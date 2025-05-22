package se.sundsvall.messaging.integration.db.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@ExtendWith(MockitoExtension.class)
class ProjectionUtilTest {

	@Test
	void createStatsProjection() {
		var projectionMock = Mockito.mock(StatsProjection.class);
		when(projectionMock.getStatus()).thenReturn(MessageStatus.SENT);
		when(projectionMock.getOriginalMessageType()).thenReturn(MessageType.SMS);
		when(projectionMock.getMessageType()).thenReturn(MessageType.SNAIL_MAIL);

		var result = ProjectionUtil.overrideStatsProjection(projectionMock, "origin", "department", "municipalityId");

		assertThat(result.getStatus()).isEqualTo(MessageStatus.SENT);
		assertThat(result.getOriginalMessageType()).isEqualTo(MessageType.SMS);
		assertThat(result.getMessageType()).isEqualTo(MessageType.SNAIL_MAIL);
		assertThat(result.getOrigin()).isEqualTo("origin");
		assertThat(result.getDepartment()).isEqualTo("department");
		assertThat(result.getMunicipalityId()).isEqualTo("municipalityId");

	}
}
