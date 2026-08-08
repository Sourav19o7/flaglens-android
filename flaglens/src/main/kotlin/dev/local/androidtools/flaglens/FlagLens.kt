package dev.local.androidtools.flaglens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import dev.local.androidtools.flaglens.internal.ContextStore
import dev.local.androidtools.flaglens.internal.FlagQuery
import dev.local.androidtools.flaglens.internal.FlagRegistry
import dev.local.androidtools.flaglens.internal.FlagReportSerializer
import dev.local.androidtools.flaglens.internal.Masker
import dev.local.androidtools.flaglens.model.ContextEntry
import dev.local.androidtools.flaglens.model.Flag
import dev.local.androidtools.flaglens.model.FlagReport
import dev.local.androidtools.flaglens.model.OverrideAuditEntry
import dev.local.androidtools.flaglens.ui.FlagLensActivity

/**
 * FlagLens's public entry point: register flags/context, read the current merged view, manage
 * local overrides (gated behind two independent safety checks — see [FlagLensConfig]), and open
 * the debug panel.
 */
object FlagLens {
    @Volatile
    private var config: FlagLensConfig? = null

    @Volatile
    private var registry: FlagRegistry? = null

    @Volatile
    private var contextStore: ContextStore? = null

    fun initialize(context: Context, config: FlagLensConfig) {
        this.config = config
        val masker = Masker(config.maskingEnabled, config.additionalSensitiveKeys)
        this.registry = FlagRegistry(masker, config.maxAuditEntries)
        this.contextStore = ContextStore(masker)
    }

    fun isEnabled(): Boolean = config?.enabled == true

    fun currentConfig(): FlagLensConfig? = config

    /** Registers (or updates) a single flag value directly — the "push" path. */
    fun registerFlag(key: String, value: Any, source: String, metadata: Map<String, String> = emptyMap()) {
        if (!isEnabled()) return
        registry?.registerFlag(key, value, source, metadata, now())
    }

    /** Registers a live, pull-based flag source under [name] — see [FlagProvider]. */
    fun registerProvider(name: String, provider: FlagProvider) {
        if (!isEnabled()) return
        registry?.registerProvider(name, provider)
    }

    fun unregisterProvider(name: String) {
        registry?.unregisterProvider(name)
    }

    fun setContext(key: String, value: String) {
        if (!isEnabled()) return
        contextStore?.set(key, value, now())
    }

    fun contextSnapshot(): List<ContextEntry> = contextStore?.snapshot().orEmpty()

    /** Every pushed flag plus every provider's current flags, merged, with override/masking state applied. */
    fun allFlags(): List<Flag> = registry?.snapshot().orEmpty()

    fun search(query: String, flags: List<Flag> = allFlags()): List<Flag> = FlagQuery.search(flags, query)

    fun groupedBySource(flags: List<Flag> = allFlags()): Map<String, List<Flag>> = FlagQuery.groupBySource(flags)

    /**
     * True only when both [FlagLensConfig.enabled] and [FlagLensConfig.allowLocalOverrides] are
     * `true`. A standard `enabled = BuildConfig.DEBUG` release build can never satisfy this,
     * regardless of [allowLocalOverrides]'s value — that's the "impossible to accidentally enable
     * in release" guarantee.
     */
    fun overridesAllowed(): Boolean = isEnabled() && config?.allowLocalOverrides == true

    /** Sets a local override for [key]. No-ops (returns `false`) unless [overridesAllowed]. */
    fun setOverride(key: String, value: String): Boolean {
        if (!overridesAllowed()) return false
        registry?.setOverride(key, value, now())
        return true
    }

    fun clearOverride(key: String) {
        registry?.clearOverride(key, now())
    }

    fun clearAllOverrides() {
        registry?.clearAllOverrides(now())
    }

    /** Session-scoped record of every override set/cleared. Never persisted, cleared on [reset]. */
    fun auditTrail(): List<OverrideAuditEntry> = registry?.auditTrail().orEmpty()

    fun exportJson(): String = FlagReportSerializer.toJson(buildReport())

    fun exportMarkdown(): String = FlagReportSerializer.toMarkdown(buildReport())

    fun copyToClipboard(context: Context, text: String, label: String = "FlagLens report") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** Launches [FlagLensActivity]. No-ops when FlagLens is disabled. */
    fun show(context: Context) {
        if (!isEnabled()) return
        val intent = Intent(context, FlagLensActivity::class.java)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /** Test-only: fully resets FlagLens to its uninitialized state. */
    fun reset() {
        config = null
        registry = null
        contextStore = null
    }

    private fun buildReport(): FlagReport {
        val cfg = config
        return FlagReport(
            appName = cfg?.appName.orEmpty(),
            environment = cfg?.environment.orEmpty(),
            generatedAtMs = now(),
            context = contextSnapshot(),
            flags = allFlags(),
        )
    }

    private fun now(): Long = System.currentTimeMillis()
}
