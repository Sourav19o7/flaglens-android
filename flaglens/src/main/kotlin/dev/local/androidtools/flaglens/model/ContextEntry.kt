package dev.local.androidtools.flaglens.model

import kotlinx.serialization.Serializable

@Serializable
data class ContextEntry(
    val key: String,
    val value: String,
    val updatedAtMs: Long,
    val isMasked: Boolean = false,
) {
    val displayValue: String get() = if (isMasked) "[MASKED]" else value
}
