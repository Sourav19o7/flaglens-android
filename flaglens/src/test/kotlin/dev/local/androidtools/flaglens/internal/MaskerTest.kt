package dev.local.androidtools.flaglens.internal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MaskerTest {

    @Test
    fun `matches default sensitive keys regardless of separator style`() {
        val masker = Masker(enabled = true, additionalSensitiveKeys = emptySet())
        assertTrue(masker.isSensitive("api_key"))
        assertTrue(masker.isSensitive("X-Api-Key"))
        assertTrue(masker.isSensitive("ApiKey"))
        assertTrue(masker.isSensitive("Authorization"))
        assertFalse(masker.isSensitive("checkout_experiment"))
    }

    @Test
    fun `additional sensitive keys are honored`() {
        val masker = Masker(enabled = true, additionalSensitiveKeys = setOf("internal_user_id"))
        assertTrue(masker.isSensitive("internal_user_id"))
    }

    @Test
    fun `disabled masker never flags anything sensitive`() {
        val masker = Masker(enabled = false, additionalSensitiveKeys = emptySet())
        assertFalse(masker.isSensitive("password"))
    }
}
