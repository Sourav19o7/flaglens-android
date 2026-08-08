package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.model.Flag

/** Pure query helpers over an already-built [Flag] list — no locking needed, no side effects. */
internal object FlagQuery {
    fun search(flags: List<Flag>, query: String): List<Flag> {
        if (query.isBlank()) return flags
        val needle = query.trim().lowercase()
        return flags.filter { it.key.lowercase().contains(needle) || it.source.lowercase().contains(needle) }
    }

    fun groupBySource(flags: List<Flag>): Map<String, List<Flag>> =
        flags.groupBy { it.source }.toSortedMap()
}
