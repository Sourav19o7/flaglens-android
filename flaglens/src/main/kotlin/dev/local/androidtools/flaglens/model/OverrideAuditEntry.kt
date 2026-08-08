package dev.local.androidtools.flaglens.model

import kotlinx.serialization.Serializable

enum class OverrideAction { SET, CLEAR, CLEAR_ALL }

/** One entry in the session-scoped audit trail of override changes. Never persisted to disk. */
@Serializable
data class OverrideAuditEntry(
    val timestampMs: Long,
    val key: String,
    val action: OverrideAction,
    val newValue: String? = null,
)
