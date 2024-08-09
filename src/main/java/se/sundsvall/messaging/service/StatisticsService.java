package se.sundsvall.messaging.service;

import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStatisticsList;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toStatistics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

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

	public Statistics getStatistics(final MessageType messageType, final LocalDate from,
		final LocalDate to, final String municipalityId) {
		return toStatistics(dbIntegration.getStats(messageType, from, to, municipalityId));
	}

	public List<DepartmentStatistics> getDepartmentLetterStatistics(final String origin, final String department, final LocalDate from,
		final LocalDate to, final String municipalityId) {
		return toDepartmentStatisticsList(dbIntegration.getStatsByMunicipalityIdAndOriginAndDepartment(municipalityId, origin, department, LETTER, from, to), municipalityId);
	}

}
