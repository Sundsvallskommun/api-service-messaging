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
import se.sundsvall.messaging.integration.db.entity.StatisticEntity;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;

public class StatisticsMapper {

	private static final String UNCATEGORIZED_DEPARTMENT = "Ej kategoriserad";
	private static final String UNCATEGORIZED_ORIGIN = "Other";

	private StatisticsMapper() {}

	public static Statistics toStatistics(final List<StatisticEntity> stats) {
		final var messageAndLetterEntries = stats.stream()
			.filter(entry -> EnumSet.of(MESSAGE, LETTER).contains(entry.getOriginalMessageType()))
			.toList();

		final var otherEntries = stats.stream()
			.filter(entry -> !EnumSet.of(MESSAGE, LETTER).contains(entry.getOriginalMessageType()))
			.toList();

		return Statistics.builder()
			.withMessage(mapToMessage(messageAndLetterEntries))
			.withLetter(mapToLetter(messageAndLetterEntries))
			.withWebMessage(mapToCount(otherEntries, WEB_MESSAGE))
			.withDigitalMail(mapToCount(otherEntries, DIGITAL_MAIL))
			.withSnailMail(mapToCount(otherEntries, SNAIL_MAIL))
			.withSms(mapToCount(otherEntries, SMS))
			.withEmail(mapToCount(otherEntries, EMAIL))
			.build();
	}

	public static List<DepartmentStatistics> toDepartmentStatisticsList(final List<StatisticEntity> stats, final String municipalityId) {

		final var statsWithUncategorized = stats.stream()
			.filter(entry -> entry.getOriginalMessageType() == LETTER)
			.map(entry -> isEmpty(entry.getOrigin()) ? overrideEntryValues(entry, UNCATEGORIZED_ORIGIN, entry.getDepartment(), municipalityId) : entry)
			.map(entry -> isEmpty(entry.getDepartment()) ? overrideEntryValues(entry, entry.getOrigin(), UNCATEGORIZED_DEPARTMENT, municipalityId) : entry)
			.toList();

		final Map<String, Map<String, Map<MessageType, Map<MessageStatus, Integer>>>> letterStats = statsWithUncategorized.stream()
			.filter(entry -> entry.getOriginalMessageType() == LETTER && isNotEmpty(entry.getDepartment()) && isNotEmpty(entry.getOrigin()))
			.collect(groupingBy(StatisticEntity::getOrigin,
				groupingBy(StatisticEntity::getDepartment,
					groupingBy(StatisticEntity::getMessageType,
						groupingBy(StatisticEntity::getStatus, summingInt(n -> 1))))));

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
	public static DepartmentStats toDepartmentStats(final List<StatisticEntity> statsEntries, final String department, final String origin) {
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
	static Count mapToCount(final List<StatisticEntity> statsEntries, final MessageType messageType) {
		final var typeEntries = statsEntries.stream()
			.filter(entry -> entry.getMessageType() == messageType)
			.toList();

		if (typeEntries.isEmpty()) {
			return null;
		}

		final var success = Math.toIntExact(typeEntries.stream()
			.filter(entry -> entry.getStatus() == SENT)
			.count());
		final var failed = Math.toIntExact(typeEntries.stream()
			.filter(entry -> entry.getStatus() == FAILED)
			.count());

		return new Count(success, failed);
	}

	static Statistics.Message mapToMessage(final List<StatisticEntity> statsEntries) {
		final var messageEntries = statsEntries.stream()
			.filter(entry -> entry.getOriginalMessageType() == MESSAGE)
			.toList();

		if (messageEntries.isEmpty()) {
			return null;
		}

		final var sms = mapToCount(messageEntries, SMS);
		final var email = mapToCount(messageEntries, EMAIL);

		final var undeliverable = Math.toIntExact(messageEntries.stream()
			.filter(entry -> entry.getMessageType() == MESSAGE)
			.count());

		return new Statistics.Message(email, sms, undeliverable);
	}

	static Statistics.Letter mapToLetter(final List<StatisticEntity> statsEntries) {
		final var letterEntries = statsEntries.stream()
			.filter(entry -> entry.getOriginalMessageType() == LETTER)
			.toList();

		if (letterEntries.isEmpty()) {
			return null;
		}

		final var snailMail = mapToCount(letterEntries, SNAIL_MAIL);
		final var digitalMail = mapToCount(letterEntries, DIGITAL_MAIL);

		return new Statistics.Letter(snailMail, digitalMail);
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

	static StatisticEntity overrideEntryValues(StatisticEntity original, String origin, String department, String municipalityId) {
		return StatisticEntity.builder()
			.withMessageType(original.getMessageType())
			.withOriginalMessageType(original.getOriginalMessageType())
			.withStatus(original.getStatus())
			.withOrigin(origin)
			.withDepartment(department)
			.withMunicipalityId(municipalityId)
			.build();
	}

}
