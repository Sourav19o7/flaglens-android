package dev.local.androidtools.flaglens

import org.junit.Assert.assertThrows
import org.junit.Test

class FlagLensConfigTest {

    @Test
    fun `overrides are disabled by default`() {
        val config = FlagLensConfig(enabled = true, appName = "App", environment = "staging")
        assert(!config.allowLocalOverrides)
    }

    @Test
    fun `rejects blank app name`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlagLensConfig(enabled = true, appName = "  ", environment = "staging")
        }
    }

    @Test
    fun `rejects non-positive maxAuditEntries`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlagLensConfig(enabled = true, appName = "App", environment = "staging", maxAuditEntries = 0)
        }
    }
}
