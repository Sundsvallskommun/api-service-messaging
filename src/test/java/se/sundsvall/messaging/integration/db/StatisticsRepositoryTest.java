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
import se.sundsvall.messaging.integration.db.entity.StatisticEntity;
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
	void findAllByParameters() {
		final var statisticsEntities = statisticsRepository.findAllByParameters(null, null, null, List.of(SMS), null, null);

		// Assert that the map contains the expected keys
		assertThat(statisticsEntities).extracting(StatisticEntity::getMessageType, StatisticEntity::getOriginalMessageType, StatisticEntity::getStatus)
			.containsExactly(
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, FAILED),
				tuple(SMS, SMS, SENT),
				tuple(SMS, SMS, FAILED));
	}

	@Test
	void findAllByParametersWithFromAndTo() {
		final var statisticsEntities = statisticsRepository.findAllByParameters(null, null, null, List.of(SMS), LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 25));

		// Assert that the map contains the expected keys
		assertThat(statisticsEntities).extracting(StatisticEntity::getMessageType, StatisticEntity::getOriginalMessageType, StatisticEntity::getStatus)
			.containsExactly(
				tuple(SMS, SMS, SENT));
	}

	@ParameterizedTest()
	@MethodSource("provideDateParameters")
	void findAllByParametersWithMunicipalityIdAndyOriginAndDepartment(LocalDate from, LocalDate to) {
		final var statisticsEntities = statisticsRepository.findAllByParameters("2281", "origin1", "SBK(Gatuavdelningen, Trafiksektionen)", List.of(LETTER), from, to);

		// Assert that the map contains the expected keys
		assertThat(statisticsEntities).extracting(StatisticEntity::getDepartment, StatisticEntity::getMessageType, StatisticEntity::getOriginalMessageType, StatisticEntity::getStatus)
			.containsExactly(
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED));
	}

	@ParameterizedTest()
	@MethodSource("provideDateParameters")
	void findAllByParametersWithDepartment(LocalDate from, LocalDate to) {

		final var statisticsEntities = statisticsRepository.findAllByParameters("2281", null, "SBK(Gatuavdelningen, Trafiksektionen)", List.of(LETTER), from, to);

		// Assert that the map contains the expected keys
		assertThat(statisticsEntities).extracting(StatisticEntity::getDepartment, StatisticEntity::getMessageType, StatisticEntity::getOriginalMessageType, StatisticEntity::getStatus)
			.containsExactly(
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED));
	}

	@Test
	void findAllByParametersWithDepartmentNoOriginAndDepartment() {

		final var statisticsEntities = statisticsRepository.findAllByParameters("2281", null, null, List.of(LETTER), null, null);

		// Assert that the map contains the expected keys
		assertThat(statisticsEntities).extracting(StatisticEntity::getDepartment, StatisticEntity::getMessageType, StatisticEntity::getOriginalMessageType, StatisticEntity::getStatus)
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
	void findAllByParametersWithMunicipalityIdAndDepartmentAndOriginAndMessageTypes() {
		final var statisticsEntities = statisticsRepository.findAllByParameters("2281", null, "SBK(Gatuavdelningen, Trafiksektionen)", List.of(LETTER, SMS), LocalDate.of(2022, 1, 1), LocalDate.of(2024, 12, 25));

		assertThat(statisticsEntities).extracting(StatisticEntity::getMessageType, StatisticEntity::getOriginalMessageType, StatisticEntity::getDepartment, StatisticEntity::getStatus)
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
