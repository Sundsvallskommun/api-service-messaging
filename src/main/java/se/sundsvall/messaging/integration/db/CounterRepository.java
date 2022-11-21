package se.sundsvall.messaging.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.messaging.integration.db.entity.CounterEntity;

public interface CounterRepository extends JpaRepository<CounterEntity, String> {

    default Optional<CounterEntity> findByName(String name) {
        return findById(name);
    }
}
