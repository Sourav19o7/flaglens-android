package dev.local.androidtools.flaglens.internal

import dev.local.androidtools.flaglens.FlagProvider
import dev.local.androidtools.flaglens.model.FlagValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlagRegistryTest {

    private fun registry() = FlagRegistry(Masker(enabled = true, additionalSensitiveKeys = emptySet()), maxAuditEntries = 10)

    @Test
    fun `registered flag appears in snapshot with correct source`() {
        val reg = registry()
        reg.registerFlag("new_checkout", true, "firebase_remote_config", emptyMap(), 1L)

        val flag = reg.snapshot().single()
        assertEquals("new_checkout", flag.key)
        assertEquals("true", flag.actualValue.raw)
        assertEquals("firebase_remote_config", flag.source)
    }

    @Test
    fun `provider flags are queried live on every snapshot`() {
        val reg = registry()
        var currentValue = "control"
        reg.registerProvider("experiments") { mapOf("checkout_variant" to FlagValue.of(currentValue)) }

        assertEquals("control", reg.snapshot().single().actualValue.raw)

        currentValue = "variant_b"
        assertEquals("variant_b", reg.snapshot().single().actualValue.raw)
    }

    @Test
    fun `unregistered provider no longer contributes flags`() {
        val reg = registry()
        reg.registerProvider("x") { mapOf("a" to FlagValue.of("1")) }
        reg.unregisterProvider("x")

        assertTrue(reg.snapshot().isEmpty())
    }

    @Test
    fun `override marks the flag as overridden without changing the actual value`() {
        val reg = registry()
        reg.registerFlag("theme", "light", "static", emptyMap(), 1L)
        reg.setOverride("theme", "dark", 2L)

        val flag = reg.snapshot().single()
        assertTrue(flag.isOverridden)
        assertEquals("dark", flag.overrideValue)
        assertEquals("light", flag.actualValue.raw)
        assertEquals("dark", flag.effectiveValue)
    }

    @Test
    fun `clearing override reverts to the actual value`() {
        val reg = registry()
        reg.registerFlag("theme", "light", "static", emptyMap(), 1L)
        reg.setOverride("theme", "dark", 2L)
        reg.clearOverride("theme", 3L)

        val flag = reg.snapshot().single()
        assertFalse(flag.isOverridden)
        assertEquals("light", flag.effectiveValue)
    }

    @Test
    fun `sensitive flag keys are masked in the snapshot`() {
        val reg = registry()
        reg.registerFlag("api_key", "super-secret", "static", emptyMap(), 1L)

        val flag = reg.snapshot().single()
        assertTrue(flag.isMasked)
        assertEquals("[MASKED]", flag.displayValue)
    }

    @Test
    fun `audit trail records set, clear and clear-all actions`() {
        val reg = registry()
        reg.registerFlag("a", "1", "static", emptyMap(), 1L)
        reg.setOverride("a", "2", 2L)
        reg.clearOverride("a", 3L)
        reg.setOverride("a", "3", 4L)
        reg.clearAllOverrides(5L)

        val actions = reg.auditTrail().map { it.action }
        assertEquals(4, actions.size)
    }
}
