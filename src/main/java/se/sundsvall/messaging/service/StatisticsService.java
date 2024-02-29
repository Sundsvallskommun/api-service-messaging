package se.sundsvall.messaging.service;

import org.springframework.stereotype.Service;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.service.mapper.StatisticsMapper;

import java.time.LocalDate;

import static se.sundsvall.messaging.model.MessageType.LETTER;

@Service
public class StatisticsService {

    private final DbIntegration dbIntegration;
    private final StatisticsMapper statisticsMapper;

    public StatisticsService(final DbIntegration dbIntegration,
            final StatisticsMapper statisticsMapper) {
        this.dbIntegration = dbIntegration;
        this.statisticsMapper = statisticsMapper;
    }

    public Statistics getStatistics(final MessageType messageType, final LocalDate from,
            final LocalDate to) {
        return statisticsMapper.toStatistics(dbIntegration.getStats(messageType, from, to));
    }

    public DepartmentStatistics getDepartmentLetterStatistics(final String department, final LocalDate from,
            final LocalDate to) {
        return statisticsMapper.toDepartmentStatistics(dbIntegration.getStatsByDepartment(department, LETTER, from, to));
    }
}
