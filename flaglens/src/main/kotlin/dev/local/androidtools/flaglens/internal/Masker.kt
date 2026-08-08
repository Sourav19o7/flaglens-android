package dev.local.androidtools.flaglens.internal

private val DEFAULT_SENSITIVE_KEYS = setOf(
    "token", "access_token", "refresh_token", "authorization", "cookie",
    "password", "passwd", "secret", "api_key", "apikey",
)

/**
 * Decides whether a flag/context key should be masked in the panel. Same normalized
 * contains-matching approach as ReproKit's `Redactor` (see that project's `HOW_IT_WORKS.md` for
 * why): keys are stripped of separators and lowercased before comparison, so `X-Api-Key`,
 * `api_key`, and `apiKey` all match the same rule regardless of naming convention.
 */
internal class Masker(private val enabled: Boolean, additionalSensitiveKeys: Set<String>) {
    private val sensitiveKeys: Set<String> = DEFAULT_SENSITIVE_KEYS + additionalSensitiveKeys.map { it.lowercase() }

    fun isSensitive(key: String): Boolean {
        if (!enabled) return false
        val normalizedKey = normalize(key)
        return sensitiveKeys.any { normalizedKey.contains(normalize(it)) }
    }

    private fun normalize(value: String): String = value.lowercase().filter { it.isLetterOrDigit() }
}
