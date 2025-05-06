package se.sundsvall.messaging.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.test.annotation.UnitTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@UnitTest
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-ut.sql"
})
class StatisticsRepositoryTest {

	@Autowired
	private StatisticsRepository statisticsRepository;

	private static Stream<Arguments> provideDateParameters() {
		return Stream.of(
			Arguments.of(LocalDate.of(2024, 1, 1), LocalDate.now()),
			Arguments.of(LocalDate.of(2024, 1, 1), null),
			Arguments.of(null, LocalDate.now()),
			Arguments.of(null, null));
	}

	@Test
	void getStats() {

		final var statsEntries = statisticsRepository.getStats(SMS, null, null, null);

		// Assert that the map contains the expected keys
		assertThat(statsEntries).extracting(StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, FAILED),
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, FAILED));
	}

	@Test
	void getStatsWithFromAndTo() {

		final var statsEntries = statisticsRepository.getStats(SMS, LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 25), "2281");

		// Assert that the map contains the expected keys
		assertThat(statsEntries).extracting(StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple(SMS, SMS, SENT));
	}

	@ParameterizedTest()
	@MethodSource("provideDateParameters")
	void getStatsByMunicipalityIdAndyOriginAndDepartment(LocalDate from, LocalDate to) {

		final var origin = "origin1";
		final var historyEntities = statisticsRepository.getStatsByMunicipalityIdAndyOriginAndDepartment("2281", origin, "SBK(Gatuavdelningen, Trafiksektionen)", LETTER, from, to);

		// Assert that the map contains the expected keys
		assertThat(historyEntities).extracting(StatsEntry::department, StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED));
	}

	@ParameterizedTest()
	@MethodSource("provideDateParameters")
	void getStatsByDepartment(LocalDate from, LocalDate to) {

		final var historyEntities = statisticsRepository.getStatsByMunicipalityIdAndyOriginAndDepartment("2281", null, "SBK(Gatuavdelningen, Trafiksektionen)", LETTER, from, to);

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

		final var historyEntities = statisticsRepository.getStatsByMunicipalityIdAndyOriginAndDepartment("2281", null, null, LETTER, null, null);

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

	@Test
	void getStatsByMunicipalityIdAndDepartmentAndOriginAndMessageTypes() {
		var statEntries = statisticsRepository.getStatsByMunicipalityIdAndDepartmentAndOriginAndMessageTypes("2281", "SBK(Gatuavdelningen, Trafiksektionen)", null, List.of(LETTER, SMS), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 25));

		assertThat(statEntries).extracting(StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::department, StatsEntry::status)
			.containsExactlyInAnyOrder(
				tuple(SNAIL_MAIL, LETTER, "SBK(Gatuavdelningen, Trafiksektionen)", SENT),
				tuple(SNAIL_MAIL, LETTER, "SBK(Gatuavdelningen, Trafiksektionen)", SENT),
				tuple(SNAIL_MAIL, LETTER, "SBK(Gatuavdelningen, Trafiksektionen)", SENT),
				tuple(SNAIL_MAIL, LETTER, "SBK(Gatuavdelningen, Trafiksektionen)", FAILED),
				tuple(SMS, SMS, "SBK(Gatuavdelningen, Trafiksektionen)", SENT),
				tuple(SMS, SMS, "SBK(Gatuavdelningen, Trafiksektionen)", FAILED),
				tuple(SMS, SMS, "SBK(Gatuavdelningen, Trafiksektionen)", SENT),
				tuple(SMS, SMS, "SBK(Gatuavdelningen, Trafiksektionen)", FAILED));
	}

}
