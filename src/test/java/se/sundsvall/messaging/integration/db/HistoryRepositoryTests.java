package se.sundsvall.messaging.integration.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@ActiveProfiles("junit")
@DataJpaTest
class HistoryRepositoryTests {
    
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();
    private static final String PARTY_ID = UUID.randomUUID().toString();

    @Autowired
    private HistoryRepository repository;

    @BeforeEach
    void setUp() {
        var entries = List.of(
            createHistory(history -> history.setBatchId(BATCH_ID)),
            createHistory(history -> history.setBatchId(BATCH_ID)),
            createHistory(history -> history.setMessageId(MESSAGE_ID)),
            createHistory(history -> history.setPartyId(PARTY_ID))
        );

        repository.saveAll(entries);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void findByMessageId_whenHistoryWithMessageIdExist_thenIsPresent() {
        var optionalHistoryEntity = repository.findByMessageIdEquals(MESSAGE_ID);

        assertThat(optionalHistoryEntity).isPresent()
            .hasValueSatisfying(history -> assertThat(history.getMessageId()).isEqualTo(MESSAGE_ID));
    }

    @Test
    void findByMessageId_whenNoHistoryWithMessageIdExist_thenEmptyResult() {
        var optionalHistoryEntity = repository.findByMessageIdEquals("unknown id");

        assertThat(optionalHistoryEntity).isEmpty();
    }

    @Test
    void findByBatchId_whenHistoryEntriesWithBatchIdExist_thenReturnsMatches() {
        var entries = repository.findByBatchIdEquals(BATCH_ID);

        assertThat(entries).hasSize(2)
                .allMatch(history -> Objects.equals(history.getBatchId(), BATCH_ID));
    }

    @Test
    void findByBatchId_whenNoHistoryEntriesExist_thenEmptyResultList() {
        var historyEntities = repository.findByBatchIdEquals("unknown id");

        assertThat(historyEntities).isEmpty();
    }

    @Test
    void findByPartyIdEquals_givenPartyId_thenReturnListOfEntityWithValidPartyId() {
        assertThat(repository.findByPartyIdEquals(PARTY_ID)).hasSize(1);
        assertThat(repository.findByPartyIdEquals(PARTY_ID).get(0).getPartyId()).isEqualTo(PARTY_ID);
    }

    @Test
    void findByPartyIdEquals_givenNonExistingPartyId_thenReturnEmptyList() {
        assertThat(repository.findByPartyIdEquals("123")).isEmpty();
    }

    private HistoryEntity createHistory(Consumer<HistoryEntity> modifier) {
        var historyEntity = HistoryEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(UUID.randomUUID().toString())
            .withSender("Sender name")
            .withMessage("Message content")
            .withPartyContact("john.doe@example.com")
            .withMessageType(MessageType.EMAIL)
            .withStatus(MessageStatus.SENT)
            .withCreatedAt(LocalDateTime.now())
            .build();

        if (modifier != null) {
            modifier.accept(historyEntity);
        }

        return historyEntity;
    }
}
