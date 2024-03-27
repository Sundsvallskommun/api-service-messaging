package se.sundsvall.messaging.integration.db.mapper;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class StatsEntryMapper {

	private StatsEntryMapper() {
	}

	public static StatsEntry toStatsEntry(final HistoryEntity historyEntity) {
		return ofNullable(historyEntity)
			.map(entity -> new StatsEntry(entity.getMessageType(), entity.getOriginalMessageType(), entity.getStatus(), entity.getOrigin(), entity.getDepartment()))
			.orElse(null);
	}

	public static List<StatsEntry> toStatsEntries(final List<HistoryEntity> historyEntities) {
		return ofNullable(historyEntities).orElse(emptyList()).stream()
			.map(StatsEntryMapper::toStatsEntry)
			.toList();
	}
}
