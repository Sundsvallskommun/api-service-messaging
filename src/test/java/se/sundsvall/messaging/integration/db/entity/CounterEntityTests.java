package se.sundsvall.messaging.integration.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class CounterEntityTests {

    @Test
    void test_valueIsSetToZeroByDefault() {
        var counter = CounterEntity.builder().build();

        assertThat(counter.getValue()).isZero();
    }

    @Test
    void testIncrement() {
        var counter = CounterEntity.builder().build()
            .increment()
            .increment();

        assertThat(counter.getValue()).isEqualTo(2);
    }

    @Test
    void testDecrement() {
        var counter = CounterEntity.builder().withValue(3).build()
            .decrement()
            .decrement();

        assertThat(counter.getValue()).isEqualTo(1);

        counter = counter.decrement().decrement();

        assertThat(counter.getValue()).isZero();
    }
}
