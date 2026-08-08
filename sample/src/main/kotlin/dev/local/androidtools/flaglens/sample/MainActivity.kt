package dev.local.androidtools.flaglens.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import dev.local.androidtools.flaglens.sample.ui.theme.FlagLensSampleTheme
import dev.local.androidtools.flaglens.ui.FlagLensPanel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagLensSampleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SampleRoot()
                }
            }
        }
    }
}

@Composable
private fun SampleRoot() {
    var showEmbeddedPanel by remember { mutableStateOf(false) }

    if (showEmbeddedPanel) {
        // Demonstrates embedding FlagLensPanel() directly in your own navigation, as an
        // alternative to FlagLens.show(context) launching a separate Activity.
        FlagLensPanel(onClose = { showEmbeddedPanel = false })
    } else {
        HomeScreen(onOpenEmbeddedPanel = { showEmbeddedPanel = true })
    }
}

@Composable
private fun HomeScreen(onOpenEmbeddedPanel: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FlagLensSampleApp

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("FlagLens Sample", style = MaterialTheme.typography.headlineSmall)
        Text(
            "DEBUG BUILD ONLY — this app registers example flags, an experiment provider, and runtime context on startup.",
            color = Color(0xFFD32F2F),
            style = MaterialTheme.typography.bodySmall,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("FlagLens status: " + if (FlagLens.isEnabled()) "enabled" else "disabled", style = MaterialTheme.typography.bodyMedium)
                Text("Registered flags: ${FlagLens.allFlags().size}", style = MaterialTheme.typography.bodySmall)
                Text("Overrides allowed: ${FlagLens.overridesAllowed()}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(onClick = { FlagLens.show(context) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open FlagLens panel (Activity)")
        }

        Button(onClick = onOpenEmbeddedPanel, modifier = Modifier.fillMaxWidth()) {
            Text("Open FlagLens panel (embedded Compose)")
        }

        Button(onClick = { app.experimentsProvider.flipVariant() }, modifier = Modifier.fillMaxWidth()) {
            Text("Flip experiment variant (open panel to see it update)")
        }
    }
}
