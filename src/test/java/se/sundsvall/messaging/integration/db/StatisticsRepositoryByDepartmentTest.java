package se.sundsvall.messaging.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class StatisticsRepositoryByDepartmentTest {

	@Autowired
	private StatisticsRepository statisticsRepository;


	@Test
	void getStatsByDepartment() {

		final var letterStatEntries = statisticsRepository.getStatsByDepartment("SBK(Gatuavdelningen, Trafiksektionen)", LETTER, null, null);


		// Assert that the map contains the expected keys
		assertThat(letterStatEntries).extracting(StatsEntry::department, StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
			.containsExactly(
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, SENT),
				tuple("SBK(Gatuavdelningen, Trafiksektionen)", SNAIL_MAIL, LETTER, FAILED));
	}

	@Test
	void getStatsByDepartmentNoDepartment() {

		final var letterStatEntries = statisticsRepository.getStatsByDepartment(null, LETTER, null, null);


		// Assert that the map contains the expected keys
		assertThat(letterStatEntries).extracting(StatsEntry::department, StatsEntry::messageType, StatsEntry::originalMessageType, StatsEntry::status)
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

}
