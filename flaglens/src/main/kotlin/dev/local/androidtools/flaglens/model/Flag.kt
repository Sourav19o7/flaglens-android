package dev.local.androidtools.flaglens.model

import kotlinx.serialization.Serializable

/**
 * One flag as shown in the panel: its resolved (possibly masked) display value, where it came
 * from, and whether a local override is currently active. [actualValue] and [displayValue] are
 * intentionally separate — see [dev.local.androidtools.flaglens.FlagLens] KDoc for why the panel
 * never silently conflates "what the flag really is" with "what got overridden to."
 */
@Serializable
data class Flag(
    val key: String,
    val actualValue: FlagValue,
    val source: String,
    val updatedAtMs: Long,
    val isOverridden: Boolean = false,
    val overrideValue: String? = null,
    val isMasked: Boolean = false,
) {
    /** The value the app would actually observe right now: the override if present, else the actual value. */
    val effectiveValue: String get() = if (isOverridden) overrideValue.orEmpty() else displayValue

    /** The value safe to render in a UI: [FlagValue.raw] or `[MASKED]` if [isMasked]. */
    val displayValue: String get() = if (isMasked) "[MASKED]" else actualValue.raw
}
