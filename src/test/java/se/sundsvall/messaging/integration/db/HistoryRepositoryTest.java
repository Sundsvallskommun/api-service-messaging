package se.sundsvall.messaging.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-ut.sql"
})
class HistoryRepositoryTest {

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void findByMunicipalityIdAndDeliveryId() {
		// Arrange
		final var municipalityId = "2281";
		final var deliveryId = "ea6b0684-69d5-4f70-8b2d-6255303ead0e";

		// Act
		final var match = historyRepository.findByMunicipalityIdAndDeliveryId(municipalityId, deliveryId);

		// Assert
		assertThat(match).isPresent().hasValueSatisfying(entity -> {
			assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(entity.getDeliveryId()).isEqualTo(deliveryId);
			assertThat(entity.getBatchId()).isEqualTo("cb1af665-835f-45b8-8755-9aa2ed284292");
			assertThat(entity.getMessageId()).isEqualTo("d5161acb-2462-4065-a679-53b1cd77be92");
			assertThat(entity.getMessageType()).isEqualTo(SNAIL_MAIL);
			assertThat(entity.getOrigin()).isEqualTo("origin1");
			assertThat(entity.getIssuer()).isEqualTo("issuer1");
		});
	}

	@Test
	void findByMunicipalityIdAndDeliveryIdWhenNoMatch() {
		// Arrange
		final var municipalityId = "2262";
		final var deliveryId = "ea6b0684-69d5-4f70-8b2d-6255303ead0e";

		// Act and assert
		assertThat(historyRepository.findByMunicipalityIdAndDeliveryId(municipalityId, deliveryId)).isEmpty();
	}

	@Test
	void findByMunicipalityIdAndMessageId() {
		// Arrange
		final var municipalityId = "2281";
		final var messageId = "d5161acb-2462-4065-a679-53b1cd77be92";

		// Act
		final var matches = historyRepository.findByMunicipalityIdAndMessageId(municipalityId, messageId);

		// Assert
		assertThat(matches).isNotEmpty().hasSize(1)
			.extracting(
				HistoryEntity::getMunicipalityId,
				HistoryEntity::getDeliveryId,
				HistoryEntity::getBatchId,
				HistoryEntity::getMessageId,
				HistoryEntity::getMessageType,
				HistoryEntity::getOrigin,
				HistoryEntity::getIssuer)
			.containsExactly(tuple(
				municipalityId,
				"ea6b0684-69d5-4f70-8b2d-6255303ead0e",
				"cb1af665-835f-45b8-8755-9aa2ed284292",
				messageId,
				SNAIL_MAIL,
				"origin1",
				"issuer1"));
	}

	@Test
	void findByMunicipalityIdAndMessageIdWhenNoMatch() {
		// Arrange
		final var municipalityId = "2281";
		final var messageId = "c8276a4a-25ef-4f45-b89e-7802f3c45b3a";

		// Act and assert
		assertThat(historyRepository.findByMunicipalityIdAndMessageId(municipalityId, messageId)).isEmpty();
	}

	@Test
	void findByMunicipalityIdAndBatchId() {
		// Arrange
		final var municipalityId = "2281";
		final var batchId = "cb1af665-835f-45b8-8755-9aa2ed284292";

		// Act
		final var matches = historyRepository.findByMunicipalityIdAndBatchId(municipalityId, batchId);

		// Assert
		assertThat(matches).hasSize(1)
			.extracting(
				HistoryEntity::getMunicipalityId,
				HistoryEntity::getDeliveryId,
				HistoryEntity::getBatchId,
				HistoryEntity::getMessageId,
				HistoryEntity::getMessageType,
				HistoryEntity::getOrigin,
				HistoryEntity::getIssuer)
			.containsExactly(tuple(
				municipalityId,
				"ea6b0684-69d5-4f70-8b2d-6255303ead0e",
				batchId,
				"d5161acb-2462-4065-a679-53b1cd77be92",
				SNAIL_MAIL,
				"origin1",
				"issuer1"));
	}

	@Test
	void findByMunicipalityIdAndBatchIdWhenNoMatch() {
		// Arrange
		final var municipalityId = "2262";
		final var batchId = "cb1af665-835f-45b8-8755-9aa2ed284292";

		// Act and assert
		assertThat(historyRepository.findByMunicipalityIdAndBatchId(municipalityId, batchId)).isEmpty();
	}

	@Test
	void findByIssuer() {
		final var municipalityId = "2281";
		final var issuer = "issuer2";
		final var pageable = PageRequest.of(0, 100);

		final var matches = historyRepository.findByMunicipalityIdAndIssuer(municipalityId, issuer, pageable);

		assertThat(matches).hasSize(3)
			.extracting(HistoryEntity::getMunicipalityId, HistoryEntity::getDeliveryId, HistoryEntity::getBatchId, HistoryEntity::getMessageId, HistoryEntity::getOrigin, HistoryEntity::getIssuer)
			.containsExactly(
				tuple(municipalityId, "0cc1cd5b-1196-49e9-9dad-11cc0da77e3d", "b3c4bd07-8e88-4fc4-b429-d6d42b5a1a6f", "d003421e-45ea-49b4-9230-244142daa634", "origin1", "issuer2"),
				tuple(municipalityId, "754378a4-d93a-43c2-8784-d274cb8b7880", "64d78f7a-e38e-47da-8910-541044d27617", "3774daaa-3f22-4af7-93a9-7386c21210df", "origin1", "issuer2"),
				tuple(municipalityId, "2e0a24de-71bc-488c-9f1b-3cf05352bd4f", "72971e8e-e4ae-4539-9c4e-3675ae4baa37", "8fffe36f-be9d-42b9-a676-24244877c5ae", "origin2", "issuer2"));
	}

	@Test
	void existsByMunicipalityIdAndMessageIdAndIssuer() {
		final var municipalityId = "2281";
		final var messageId = "d5161acb-2462-4065-a679-53b1cd77be92";
		final var issuer = "issuer1";

		assertThat(historyRepository.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).isTrue();
	}

	@Test
	void existsByMunicipalityIdAndMessageIdAndIssuerWhenNoMatch() {
		final var municipalityId = "2262";
		final var messageId = "c8276a4a-25ef-4f45-b89e-7802f3c45b3a";
		final var issuer = "issuer1";

		assertThat(historyRepository.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).isFalse();
	}
}
