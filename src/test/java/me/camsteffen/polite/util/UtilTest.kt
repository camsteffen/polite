package me.camsteffen.polite.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilTest {
    @Test
    fun mergeSortedTest() {
        val s = listOf(
            sequenceOf(1, 3, 4),
            sequenceOf(2, 3, 5),
            sequenceOf(1, 7, 8)
        )
            .mergeSorted().toList()
        val expected = listOf(1, 1, 2, 3, 3, 4, 5, 7, 8)
        assertEquals(expected, s)
    }
}
