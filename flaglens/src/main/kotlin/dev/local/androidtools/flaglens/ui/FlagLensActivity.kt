package dev.local.androidtools.flaglens.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/** Thin host `Activity` for [FlagLensPanel], used by [dev.local.androidtools.flaglens.FlagLens.show]. */
class FlagLensActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagLensPanel(onClose = { finish() })
        }
    }
}
