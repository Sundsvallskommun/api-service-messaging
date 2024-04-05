package se.sundsvall.messaging.integration.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
class StatisticsRepositoryTests {

	@Autowired
	private StatisticsRepository statisticsRepository;


	@Test
	void getStats() {

		final var statsEntries = statisticsRepository.getStats(SMS, null, null);

		// Assert that the map contains the expected keys
		assertThat(statsEntries).extracting(StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, SENT));
	}

	@Test
	void getStatsWithFromAndTo() {

		final var statsEntries = statisticsRepository.getStats(SMS, LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 25));

		// Assert that the map contains the expected keys
		assertThat(statsEntries).extracting(StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple(SMS, SMS, SENT));
	}

	@ParameterizedTest()
	@MethodSource("provideDateParameters")
	void getStatsByOriginAndDepartment(LocalDate from, LocalDate to) {

		final var origin = "origin1";
		final var historyEntities = statisticsRepository.getStatsByOriginAndDepartment(origin, "SBK(Gatuavdelningen, Trafiksektionen)", LETTER, from, to);

		// Assert that the map contains the expected keys
		assertThat(historyEntities).extracting(StatsEntry::department, StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED));
	}

	@ParameterizedTest()
	@MethodSource("provideDateParameters")
	void getStatsByDepartment(LocalDate from, LocalDate to) {

		final var historyEntities = statisticsRepository.getStatsByOriginAndDepartment(null, "SBK(Gatuavdelningen, Trafiksektionen)", LETTER, from, to);

		// Assert that the map contains the expected keys
		assertThat(historyEntities).extracting(StatsEntry::department, StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED));
	}

	@Test
	void getStatsByDepartmentNoOriginAndDepartment() {

		final var historyEntities = statisticsRepository.getStatsByOriginAndDepartment(null, null, LETTER, null, null);

		// Assert that the map contains the expected keys
		assertThat(historyEntities).extracting(StatsEntry::department, StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactlyInAnyOrder(
				tuple("BOU Förskola", SNAIL_MAIL, LETTER, FAILED),
				tuple("Kultur och fritid", SNAIL_MAIL, LETTER, FAILED),
				tuple("Stadsbyggnadskontoret", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED),
				tuple("Kommunstyrelsekontoret", SNAIL_MAIL, LETTER, SENT),
				tuple("", SNAIL_MAIL, LETTER, SENT),
				tuple("", SNAIL_MAIL, LETTER, FAILED),
				tuple(null, SNAIL_MAIL, LETTER, SENT));
	}

	private static Stream<Arguments> provideDateParameters() {
		return Stream.of(
			Arguments.of(LocalDate.of(2024, 1, 1), LocalDate.now()),
			Arguments.of(LocalDate.of(2024, 1, 1), null),
			Arguments.of(null, LocalDate.now()),
			Arguments.of(null, null));
	}
}
