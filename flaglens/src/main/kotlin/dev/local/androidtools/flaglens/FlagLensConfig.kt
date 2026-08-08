package dev.local.androidtools.flaglens

/**
 * Configuration for [FlagLens]. As with the other tools in this project, [enabled] must be wired
 * to the host app's own debug flag — FlagLens cannot detect the build type on its own.
 *
 * Local overrides are gated by **two** independent flags — [enabled] and [allowLocalOverrides] —
 * both of which default to values that make overrides inert unless a developer deliberately turns
 * them on. This is what the README/spec means by "impossible to accidentally enable local
 * overrides in release builds unless the developer explicitly bypasses the safety check": a
 * release build with the standard `enabled = BuildConfig.DEBUG` wiring can never reach an override
 * write, because [FlagLens.setOverride] checks `enabled` first regardless of [allowLocalOverrides].
 */
data class FlagLensConfig(
    val enabled: Boolean,
    val appName: String,
    val environment: String,
    val allowLocalOverrides: Boolean = false,
    val maskingEnabled: Boolean = true,
    val additionalSensitiveKeys: Set<String> = emptySet(),
    val maxAuditEntries: Int = 100,
) {
    init {
        require(appName.isNotBlank()) { "appName must not be blank" }
        require(maxAuditEntries > 0) { "maxAuditEntries must be positive" }
    }
}
