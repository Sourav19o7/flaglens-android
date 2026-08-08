package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.model.FlagReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object FlagReportSerializer {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    fun toJson(report: FlagReport): String = json.encodeToString(report)

    fun toMarkdown(report: FlagReport): String = buildString {
        appendLine("# FlagLens report: ${report.appName}")
        appendLine()
        appendLine("Environment: `${report.environment}`")
        appendLine()

        if (report.context.isNotEmpty()) {
            appendLine("## Context")
            appendLine()
            appendLine("| Key | Value |")
            appendLine("|---|---|")
            for (entry in report.context) {
                appendLine("| ${entry.key} | ${entry.displayValue} |")
            }
            appendLine()
        }

        appendLine("## Flags (${report.flags.size})")
        appendLine()
        val bySource = report.flags.groupBy { it.source }.toSortedMap()
        for ((source, flags) in bySource) {
            appendLine("### $source")
            appendLine()
            appendLine("| Key | Value | Override | Metadata |")
            appendLine("|---|---|---|---|")
            for (flag in flags) {
                val overrideCol = if (flag.isOverridden) "→ ${flag.overrideValue}" else "-"
                val metadataCol = flag.actualValue.metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }
                appendLine("| ${flag.key} | ${flag.displayValue} | $overrideCol | $metadataCol |")
            }
            appendLine()
        }
    }
}
