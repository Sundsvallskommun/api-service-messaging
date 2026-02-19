package se.sundsvall.messaging.integration.db;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-ut.sql"
})
class MessageRepositoryTest {

	private static final String NON_EXISTING_ID = "d62134e2-f652-42a1-be43-72f620a1a358";

	@Autowired
	private MessageRepository messageRepository;

	@Test
	void findByDeliveryId() {
		// Arrange
		final var deliveryId = "0cc1cd5b-1196-49e9-9dad-11cc0da77e3d";

		// Act and assert

		assertThat(messageRepository.findByDeliveryId(deliveryId)).isPresent().hasValueSatisfying(entity -> {
			assertThat(entity.getDeliveryId()).isEqualTo(deliveryId);
			assertThat(entity.getMessageId()).isEqualTo("b3c4bd07-8e88-4fc4-b429-d6d42b5a1a6f");
			assertThat(entity.getBatchId()).isEqualTo("d003421e-45ea-49b4-9230-244142daa634");
			assertThat(entity.getStatus()).isEqualTo(SENT);
			assertThat(entity.getMunicipalityId()).isEqualTo("2281");
		});
	}

	@Test
	void findByDeliveryIdWhenNoMatch() {
		// Act and assert
		assertThat(messageRepository.findByDeliveryId(NON_EXISTING_ID)).isEmpty();
	}

	@Test
	void existsByMessageId() {
		// Arrange
		final var messageId = "b3c4bd07-8e88-4fc4-b429-d6d42b5a1a6f";

		// Act and assert
		assertThat(messageRepository.existsByMessageId(messageId)).isTrue();
	}

	@Test
	void existsByMessageIdWhenNoMatch() {
		// Act and assert
		assertThat(messageRepository.existsByMessageId(NON_EXISTING_ID)).isFalse();
	}

	@Test
	void deleteByDeliveryId() {
		// Arrange
		final var deliveryIdToDelete = "abd1c62e-8242-4a08-b6ab-edffb3a51235";

		// Act and assert
		assertThat(messageRepository.findByDeliveryId(deliveryIdToDelete)).isPresent();
		messageRepository.deleteByDeliveryId(deliveryIdToDelete);
		assertThat(messageRepository.findByDeliveryId(deliveryIdToDelete)).isEmpty();
	}

	@Test
	void findByStatusOrderByCreatedAtAsc() {
		// Act and assert
		final var matches = messageRepository.findByStatusOrderByCreatedAtAsc(PENDING);

		assertThat(matches).hasSize(2).satisfiesExactly(entity -> {
			assertThat(entity.getMessageId()).isEqualTo("cb1af665-835f-45b8-8755-9aa2ed284292");
			assertThat(entity.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(LocalDateTime.of(2024, 01, 16, 14, 44, 17));
		}, entity -> {
			assertThat(entity.getMessageId()).isEqualTo("47a249ea-0308-4b93-a482-a023d708a788");
			assertThat(entity.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(LocalDateTime.of(2024, 01, 24, 10, 26, 17));
		});
	}
}
