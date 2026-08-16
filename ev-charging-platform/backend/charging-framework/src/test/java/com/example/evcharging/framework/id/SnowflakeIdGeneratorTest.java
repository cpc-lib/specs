package com.example.evcharging.framework.id;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeIdGeneratorTest {
    @Test
    void idsMustBeUniqueAndIncreasingForSingleGenerator() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = new HashSet<>();
        long previous = -1;
        for (int i = 0; i < 20_000; i++) {
            long id = generator.nextId();
            assertThat(id).isGreaterThan(previous);
            assertThat(ids.add(id)).isTrue();
            previous = id;
        }
    }
}
