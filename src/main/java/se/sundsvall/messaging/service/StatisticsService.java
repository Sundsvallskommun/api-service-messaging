package se.sundsvall.messaging.service;

import org.springframework.stereotype.Service;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;

import java.time.LocalDate;

import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStatistics;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toStatistics;

@Service
public class StatisticsService {

    private final DbIntegration dbIntegration;

    public StatisticsService(final DbIntegration dbIntegration) {
        this.dbIntegration = dbIntegration;
    }

    public Statistics getStatistics(final MessageType messageType, final LocalDate from,
            final LocalDate to) {
        return toStatistics(dbIntegration.getStats(messageType, from, to));
    }

    public DepartmentStatistics getDepartmentLetterStatistics(final String department, final LocalDate from,
            final LocalDate to) {
        return toDepartmentStatistics(dbIntegration.getStatsByDepartment(department, LETTER, from, to));
    }
}
