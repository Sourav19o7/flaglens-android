package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.FlagProvider
import dev.local.androidtools.flaglens.model.Flag
import dev.local.androidtools.flaglens.model.FlagValue
import dev.local.androidtools.flaglens.model.OverrideAction
import dev.local.androidtools.flaglens.model.OverrideAuditEntry

private data class PushedFlag(val key: String, val value: FlagValue, val source: String, val updatedAtMs: Long)
private data class Override(val value: String, val setAtMs: Long)

/**
 * The single source of truth FlagLens reads from: flags pushed directly via
 * [dev.local.androidtools.flaglens.FlagLens.registerFlag], flags pulled live from every
 * registered [FlagProvider], and any local overrides layered on top for display. All mutable
 * collections here are guarded by `@Synchronized` — flags can be registered from any thread
 * (e.g. a remote-config callback), while [snapshot] is typically called from the UI thread when
 * the panel is opened.
 */
internal class FlagRegistry(private val masker: Masker, maxAuditEntries: Int) {
    private val pushedFlags = LinkedHashMap<String, PushedFlag>()
    private val providers = LinkedHashMap<String, FlagProvider>()
    private val overrides = LinkedHashMap<String, Override>()
    private val auditTrail = CircularBuffer<OverrideAuditEntry>(maxAuditEntries)

    @Synchronized
    fun registerFlag(key: String, value: Any, source: String, metadata: Map<String, String>, timestampMs: Long) {
        pushedFlags[key] = PushedFlag(key, FlagValue.of(value, metadata), source, timestampMs)
    }

    @Synchronized
    fun registerProvider(name: String, provider: FlagProvider) {
        providers[name] = provider
    }

    @Synchronized
    fun unregisterProvider(name: String) {
        providers.remove(name)
    }

    @Synchronized
    fun setOverride(key: String, value: String, timestampMs: Long) {
        overrides[key] = Override(value, timestampMs)
        auditTrail.add(OverrideAuditEntry(timestampMs, key, OverrideAction.SET, value))
    }

    @Synchronized
    fun clearOverride(key: String, timestampMs: Long) {
        overrides.remove(key)
        auditTrail.add(OverrideAuditEntry(timestampMs, key, OverrideAction.CLEAR))
    }

    @Synchronized
    fun clearAllOverrides(timestampMs: Long) {
        overrides.clear()
        auditTrail.add(OverrideAuditEntry(timestampMs, "*", OverrideAction.CLEAR_ALL))
    }

    fun auditTrail(): List<OverrideAuditEntry> = auditTrail.snapshot()

    /**
     * Builds the current, fully-resolved flag list: every pushed flag plus every flag returned
     * right now by every registered provider, each annotated with its override/masking state.
     * Providers are queried fresh on every call — see [FlagProvider]'s KDoc for why.
     */
    @Synchronized
    fun snapshot(): List<Flag> {
        val entries = mutableListOf<Flag>()
        for (pushed in pushedFlags.values) {
            entries += toFlag(pushed.key, pushed.value, pushed.source, pushed.updatedAtMs)
        }
        for ((providerName, provider) in providers) {
            val now = System.currentTimeMillis()
            for ((key, value) in provider.getAllFlags()) {
                entries += toFlag(key, value, providerName, now)
            }
        }
        return entries
    }

    private fun toFlag(key: String, value: FlagValue, source: String, updatedAtMs: Long): Flag {
        val override = overrides[key]
        return Flag(
            key = key,
            actualValue = value,
            source = source,
            updatedAtMs = updatedAtMs,
            isOverridden = override != null,
            overrideValue = override?.value,
            isMasked = masker.isSensitive(key),
        )
    }

    @Synchronized
    fun clearAll() {
        pushedFlags.clear()
        providers.clear()
        overrides.clear()
        auditTrail.clear()
    }
}
