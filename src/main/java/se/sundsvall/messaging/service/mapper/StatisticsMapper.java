package se.sundsvall.messaging.service.mapper;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import se.sundsvall.messaging.api.model.response.DepartmentStats;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;

public class StatisticsMapper {

	private static final String UNCATEGORIZED_DEPARTMENT = "Ej kategoriserad";
	private static final String UNCATEGORIZED_ORGIN = "Other";

	private StatisticsMapper() {}

	public static Statistics toStatistics(final List<StatsEntry> stats) {
		// "Extract" MESSAGE entries, since they require special handling
		final var message = stats.stream()
			.filter(entry -> entry.originalMessageType() == MESSAGE)
			.collect(groupingBy(StatsEntry::messageType,
				groupingBy(StatsEntry::status, summingInt(n -> 1))));

		// "Extract" LETTER entries, since they require special handling
		final var letter = stats.stream()
			.filter(entry -> entry.originalMessageType() == LETTER)
			.collect(groupingBy(StatsEntry::messageType,
				groupingBy(StatsEntry::status, summingInt(n -> 1))));

		// "Extract" everything except MESSAGE and LETTER entries
		final var other = stats.stream()
			.filter(entry -> !EnumSet.of(MESSAGE, LETTER).contains(entry.originalMessageType()))
			.collect(groupingBy(StatsEntry::messageType,
				groupingBy(StatsEntry::status, summingInt(n -> 1))));

		return Statistics.builder()
			.withMessage(message.isEmpty() ? null
				: new Statistics.Message(
					// Sum up EMAIL stats
					toCount(message.get(EMAIL)),
					// Sum up SMS stats
					toCount(message.get(SMS)),
					// In the case of a message not being delivered due to contact-settings indicating
					// that either no settings have been found or that the recipient has opted-out of
					// receiving messages - treat both cases as "undeliverable" and sum them up
					ofNullable(message.get(MESSAGE))
						.map(undeliverables -> undeliverables.values().stream()
							.mapToInt(i -> i)
							.sum())
						.orElse(null)))
			.withLetter(letter.isEmpty() ? null
				: new Statistics.Letter(
					// Sum up SNAIL_MAIL stats
					toCount(letter.get(SNAIL_MAIL)),
					// Sum up DIGITAL_MAIL stats
					toCount(letter.get(DIGITAL_MAIL))))
			.withEmail(toCount(other.get(EMAIL)))
			.withSms(toCount(other.get(SMS)))
			.withWebMessage(toCount(other.get(WEB_MESSAGE)))
			.withDigitalMail(toCount(other.get(DIGITAL_MAIL)))
			.withSnailMail(toCount(other.get(SNAIL_MAIL)))
			.build();
	}

	public static List<DepartmentStatistics> toDepartmentStatisticsList(final List<StatsEntry> stats, final String municipalityId) {

		final var statsWithUncategorized = stats.stream()
			.filter(entry -> entry.originalMessageType() == LETTER)
			.map(entry -> isEmpty(entry.origin()) ? new StatsEntry(entry.messageType(), entry.originalMessageType(), entry.status(), UNCATEGORIZED_ORGIN, entry.department(), municipalityId) : entry)
			.map(entry -> isEmpty(entry.department()) ? new StatsEntry(entry.messageType(), entry.originalMessageType(), entry.status(), entry.origin(), UNCATEGORIZED_DEPARTMENT, municipalityId) : entry)
			.toList();

		final Map<String, Map<String, Map<MessageType, Map<MessageStatus, Integer>>>> letterStats = statsWithUncategorized.stream()
			.filter(entry -> entry.originalMessageType() == LETTER && isNotEmpty(entry.department()) && isNotEmpty(entry.origin()))
			.collect(groupingBy(StatsEntry::origin,
				groupingBy(StatsEntry::department,
					groupingBy(StatsEntry::messageType,
						groupingBy(StatsEntry::status, summingInt(n -> 1))))));

		return sortDepartmentStatistics(letterStats.keySet().stream().map(origin -> toDepartmentStatistics(origin, letterStats.get(origin))).toList());
	}

	/**
	 * Converts a list of {@code StatsEntry} instances into a {@code DepartmentStats} object representing the delivery
	 * statistics
	 *
	 * @param  statsEntries the list of {@code StatsEntry} objects containing message data
	 * @param  department   the department name
	 * @param  origin       the origin of the message
	 * @return              a {@code DepartmentStats} object containing the delivery statistics for the specified department
	 *                      and origin
	 */
	public static DepartmentStats toDepartmentStats(final List<StatsEntry> statsEntries, final String department, final String origin) {
		return DepartmentStats.builder()
			.withDepartment(department)
			.withOrigin(origin)
			.withSms(mapToCount(statsEntries, SMS))
			.withDigitalMail(mapToCount(statsEntries, DIGITAL_MAIL))
			.withSnailMail(mapToCount(statsEntries, SNAIL_MAIL))
			.build();
	}

	/**
	 * Converts a list of {@code StatsEntry} instances into a {@code Count} object representing the number of successful and
	 * failed message entries for the specified message type.
	 *
	 * @param  statsEntries the list of {@code StatsEntry} objects containing message data
	 * @param  messageType  the {@code MessageType} for which the count is to be calculated
	 * @return              a {@code Count} object containing the counts of sent and failed messages of the given
	 *                      {@code MessageType}
	 */
	static Count mapToCount(final List<StatsEntry> statsEntries, final MessageType messageType) {
		var typeEntries = statsEntries.stream()
			.filter(entry -> entry.messageType() == messageType)
			.toList();

		var success = Math.toIntExact(typeEntries.stream()
			.filter(entry -> entry.status() == SENT)
			.count());
		var failed = Math.toIntExact(typeEntries.stream()
			.filter(entry -> entry.status() == FAILED)
			.count());

		return new Count(success, failed);
	}

	public static Count toCount(final Map<MessageStatus, Integer> stat) {
		if (stat == null) {
			return null;
		}

		return new Count(
			ofNullable(stat.get(SENT)).orElse(0),
			ofNullable(stat.get(FAILED)).orElse(0));
	}

	private static DepartmentLetter toDepartmentLetter(final String department, final Map<MessageType, Map<MessageStatus, Integer>> letter) {
		if (letter == null) {
			return null;
		}

		return DepartmentLetter.builder()
			.withDepartment(department)
			.withSnailMail(toCount(letter.get(SNAIL_MAIL)))
			.withDigitalMail(toCount(letter.get(DIGITAL_MAIL)))
			.build();
	}

	private static DepartmentStatistics toDepartmentStatistics(final String origin, final Map<String, Map<MessageType, Map<MessageStatus, Integer>>> letters) {
		return DepartmentStatistics.builder()
			.withOrigin(origin)
			.withDepartmentLetters(letters.keySet().stream()
				.map(department -> toDepartmentLetter(department, letters.get(department)))
				.toList())
			.build();
	}

	private static List<DepartmentStatistics> sortDepartmentStatistics(final List<DepartmentStatistics> departmentStatistics) {
		final List<DepartmentStatistics> sortedOnOriginList = new ArrayList<>();
		departmentStatistics.stream()
			.filter(departmentStatistics1 -> !UNCATEGORIZED_DEPARTMENT.equals(departmentStatistics1.origin()))
			.forEach(sortedOnOriginList::add);
		departmentStatistics.stream()
			.filter(departmentStatistics1 -> UNCATEGORIZED_DEPARTMENT.equals(departmentStatistics1.origin()))
			.forEach(sortedOnOriginList::add);

		final List<DepartmentStatistics> sortedList = new ArrayList<>();
		sortedOnOriginList.stream()
			.map(departmentStatistics1 -> DepartmentStatistics.builder()
				.withOrigin(departmentStatistics1.origin())
				.withDepartmentLetters(sortDepartmentLetters(departmentStatistics1.departmentLetters()))
				.build())
			.forEach(sortedList::add);
		return sortedList;
	}

	private static List<DepartmentLetter> sortDepartmentLetters(final List<DepartmentLetter> departmentLetters) {
		final List<DepartmentLetter> sortedOnDepartmentList = new ArrayList<>();
		departmentLetters.stream()
			.filter(departmentLetter -> !UNCATEGORIZED_DEPARTMENT.equals(departmentLetter.department()))
			.forEach(sortedOnDepartmentList::add);
		departmentLetters.stream()
			.filter(departmentLetter -> UNCATEGORIZED_DEPARTMENT.equals(departmentLetter.department()))
			.forEach(sortedOnDepartmentList::add);

		return sortedOnDepartmentList;
	}

}
