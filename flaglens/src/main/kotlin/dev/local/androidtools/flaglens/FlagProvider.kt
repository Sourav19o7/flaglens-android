package dev.local.androidtools.flaglens

import dev.local.androidtools.flaglens.model.FlagValue

/**
 * A pull-based source of flags. FlagLens queries every registered provider's [getAllFlags] fresh
 * each time [FlagLens.allFlags] is called — there is no caching layer, so a provider backed by a
 * live remote-config SDK will always show the SDK's current in-memory state, not a stale snapshot.
 *
 * FlagLens ships no required implementation of this interface — not even for Firebase Remote
 * Config, which is deliberately kept out of this library's dependencies (see README's Firebase
 * adapter section for an example implementation you can copy into your own app).
 */
fun interface FlagProvider {
    fun getAllFlags(): Map<String, FlagValue>
}

/** A trivial [FlagProvider] backed by a fixed map — useful for tests, samples, or truly static flags. */
class StaticMapFlagProvider(private val flags: Map<String, FlagValue>) : FlagProvider {
    override fun getAllFlags(): Map<String, FlagValue> = flags
}
