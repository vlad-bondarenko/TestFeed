package com.example.demo.service

import spock.lang.Specification

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class IdGeneratorTest extends Specification {

    def "Id"() {
        given:
        def idGenerator = IdGenerator.INSTANCE

        final def count = 1_000
        final def ids = new long[count]
        for (i in 0..<count) {
            ids[i] = idGenerator.id()
        }

        expect: "Ids is unique and next value is always greater than the previous"
        def set = new HashSet<Long>(count)
        for (final def id in ids) {
            set.add(id)
        }

        assertEquals(count, set.size())

        def previous = 0L
        for (i in 0..<count) {
            assertTrue(ids[i] > previous)

            previous = ids[i]
        }
    }
}
