package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.model.Flag
import dev.local.androidtools.flaglens.model.FlagValue
import org.junit.Assert.assertEquals
import org.junit.Test

class FlagQueryTest {

    private fun flag(key: String, source: String) =
        Flag(key = key, actualValue = FlagValue.of("v"), source = source, updatedAtMs = 0L)

    @Test
    fun `blank query returns everything unfiltered`() {
        val flags = listOf(flag("a", "s1"), flag("b", "s2"))
        assertEquals(flags, FlagQuery.search(flags, ""))
    }

    @Test
    fun `search matches key or source case-insensitively`() {
        val flags = listOf(flag("new_checkout", "firebase"), flag("dark_mode", "static"))

        assertEquals(listOf(flags[0]), FlagQuery.search(flags, "CHECKOUT"))
        assertEquals(listOf(flags[0]), FlagQuery.search(flags, "firebase"))
        assertEquals(emptyList<Flag>(), FlagQuery.search(flags, "nope"))
    }

    @Test
    fun `groupBySource groups and sorts source names`() {
        val flags = listOf(flag("a", "zeta"), flag("b", "alpha"), flag("c", "alpha"))
        val grouped = FlagQuery.groupBySource(flags)

        assertEquals(listOf("alpha", "zeta"), grouped.keys.toList())
        assertEquals(2, grouped["alpha"]?.size)
    }
}
