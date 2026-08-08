package dev.local.androidtools.flaglens.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.local.androidtools.flaglens.FlagLens
import dev.local.androidtools.flaglens.model.Flag

/**
 * The full FlagLens debug panel: search, source grouping, override controls, and export/copy —
 * everything reads live from the [FlagLens] singleton on each recomposition, so it reflects
 * whatever the host app has registered up to this point. Safe to drop into any screen (a bottom
 * sheet, a dedicated route, a dialog) — [FlagLensActivity] is just a thin host for it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagLensPanel(onClose: (() -> Unit)? = null) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var refreshToken by remember { mutableStateOf(0) }
    val flags = remember(query, refreshToken) { FlagLens.search(query) }
    val grouped = remember(flags) { FlagLens.groupedBySource(flags) }
    val contextEntries = remember(refreshToken) { FlagLens.contextSnapshot() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlagLens") },
                actions = {
                    if (onClose != null) TextButton(onClick = onClose) { Text("Close") }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(
                "DEBUG PANEL — may expose internal configuration and flag values",
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.labelMedium,
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search flags or source") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    FlagLens.copyToClipboard(context, FlagLens.exportMarkdown())
                }) { Text("Copy Markdown") }
                OutlinedButton(onClick = {
                    FlagLens.copyToClipboard(context, FlagLens.exportJson())
                }) { Text("Copy JSON") }
                if (FlagLens.overridesAllowed()) {
                    OutlinedButton(onClick = {
                        FlagLens.clearAllOverrides()
                        refreshToken++
                    }) { Text("Reset overrides") }
                }
            }

            if (contextEntries.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Context", style = MaterialTheme.typography.titleSmall)
                        contextEntries.forEach { entry ->
                            Text("${entry.key} = ${entry.displayValue}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                grouped.forEach { (source, sourceFlags) ->
                    item {
                        Text(
                            "$source (${sourceFlags.size})",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                        HorizontalDivider()
                    }
                    items(sourceFlags) { flag ->
                        FlagRow(flag, onOverrideChanged = { refreshToken++ })
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagRow(flag: Flag, onOverrideChanged: () -> Unit) {
    var overrideInput by remember(flag.key) { mutableStateOf(flag.overrideValue ?: flag.displayValue) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(flag.key, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (flag.isOverridden) "${flag.displayValue} → ${flag.effectiveValue}" else flag.effectiveValue,
                style = MaterialTheme.typography.bodyMedium,
                color = if (flag.isOverridden) Color(0xFF1976D2) else Color.Unspecified,
            )
        }
        if (FlagLens.overridesAllowed()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = overrideInput,
                    onValueChange = { overrideInput = it },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { FlagLens.setOverride(flag.key, overrideInput); onOverrideChanged() }) { Text("Set") }
                if (flag.isOverridden) {
                    TextButton(onClick = { FlagLens.clearOverride(flag.key); onOverrideChanged() }) { Text("Clear") }
                }
            }
        }
    }
}
