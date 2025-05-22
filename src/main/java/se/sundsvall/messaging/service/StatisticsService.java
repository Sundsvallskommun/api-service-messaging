package se.sundsvall.messaging.service;

import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStatisticsList;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStats;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toStatistics;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import se.sundsvall.messaging.api.model.response.DepartmentStats;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;

@Service
public class StatisticsService {

	private final DbIntegration dbIntegration;

	public StatisticsService(final DbIntegration dbIntegration) {
		this.dbIntegration = dbIntegration;
	}

	public Statistics getStatistics(final MessageType messageType, final LocalDate from, final LocalDate to, final String municipalityId) {
		return toStatistics(dbIntegration.getStatsByParameters(municipalityId, null, null, List.of(messageType), from, to));
	}

	public List<DepartmentStatistics> getDepartmentLetterStatistics(final String origin, final String department, final LocalDate from, final LocalDate to, final String municipalityId) {
		return toDepartmentStatisticsList(dbIntegration.getStatsByParameters(municipalityId, origin, department, List.of(LETTER), from, to), municipalityId);
	}

	public DepartmentStats getStatisticsByDepartment(final String municipalityId, final String department, final String origin, final LocalDate from, final LocalDate to) {
		var statEntries = dbIntegration.getStatsByParameters(municipalityId, origin, department, List.of(LETTER, SMS), from, to);
		return toDepartmentStats(statEntries, department, origin);
	}

}
