package se.sundsvall.messaging.integration.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stats_counters")
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CounterEntity {

    @Id
    @Column(name = "counter_name", nullable = false, unique = true)
    private String name;

    @Builder.Default
    @Column(name = "counter_value", nullable = false)
    private Integer value = 0;

    public CounterEntity increment() {
        value++;

        return this;
    }

    public CounterEntity decrement() {
        value--;

        if (value < 0) {
            value = 0;
        }

        return this;
    }
}
