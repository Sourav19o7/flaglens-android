package dev.local.androidtools.flaglens.model

import kotlinx.serialization.Serializable

enum class FlagValueType { BOOLEAN, STRING, NUMBER }

/**
 * A flag's value, always stored as its `toString()` representation plus a [type] tag for display
 * purposes (so a Compose UI can render `true`/`false` differently from an arbitrary string,
 * without FlagLens needing a generic/reflective value type).
 */
@Serializable
data class FlagValue(
    val raw: String,
    val type: FlagValueType = FlagValueType.STRING,
    val metadata: Map<String, String> = emptyMap(),
) {
    companion object {
        fun of(value: Any, metadata: Map<String, String> = emptyMap()): FlagValue = when (value) {
            is Boolean -> FlagValue(value.toString(), FlagValueType.BOOLEAN, metadata)
            is Int, is Long, is Float, is Double -> FlagValue(value.toString(), FlagValueType.NUMBER, metadata)
            else -> FlagValue(value.toString(), FlagValueType.STRING, metadata)
        }
    }
}
