package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.model.ContextEntry

/** Thread-safe key-value store for runtime context (user segment, API environment, etc.). */
internal class ContextStore(private val masker: Masker) {
    private val entries = LinkedHashMap<String, ContextEntry>()

    @Synchronized
    fun set(key: String, value: String, timestampMs: Long) {
        entries.remove(key)
        entries[key] = ContextEntry(key, value, timestampMs, isMasked = masker.isSensitive(key))
    }

    @Synchronized
    fun snapshot(): List<ContextEntry> = entries.values.toList()

    @Synchronized
    fun clear() = entries.clear()
}
