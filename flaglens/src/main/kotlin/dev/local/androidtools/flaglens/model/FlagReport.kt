package dev.local.androidtools.flaglens.model

import kotlinx.serialization.Serializable

@Serializable
data class FlagReport(
    val appName: String,
    val environment: String,
    val generatedAtMs: Long,
    val context: List<ContextEntry>,
    val flags: List<Flag>,
)
