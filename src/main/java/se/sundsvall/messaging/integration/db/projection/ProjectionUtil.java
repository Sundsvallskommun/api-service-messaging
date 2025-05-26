package se.sundsvall.messaging.integration.db.projection;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

public final class ProjectionUtil {

	private ProjectionUtil() {}

	public static StatsProjection overrideStatsProjection(StatsProjection original, String origin, String department, String municipalityId) {
		return new StatsProjection() {
			@Override
			public MessageType getMessageType() {
				return original.getMessageType();
			}

			@Override
			public MessageType getOriginalMessageType() {
				return original.getOriginalMessageType();
			}

			@Override
			public MessageStatus getStatus() {
				return original.getStatus();
			}

			@Override
			public String getOrigin() {
				return origin;
			}

			@Override
			public String getDepartment() {
				return department;
			}

			@Override
			public String getMunicipalityId() {
				return municipalityId;
			}
		};
	}
}
