package dev.local.androidtools.flaglens.sample

import android.app.Application
import dev.local.androidtools.flaglens.FlagLens
import dev.local.androidtools.flaglens.FlagLensConfig
import dev.local.androidtools.flaglens.FlagProvider
import dev.local.androidtools.flaglens.model.FlagValue

/**
 * Stands in for a live remote-config SDK: its [getAllFlags] result changes over time, exactly
 * like a real `FlagProvider` backed by Firebase Remote Config, LaunchDarkly, etc. would.
 * FlagLens re-queries this on every panel refresh — see FlagProvider's KDoc.
 */
internal class ExampleExperimentsProvider : FlagProvider {
    @Volatile private var variant = "control"

    fun flipVariant() {
        variant = if (variant == "control") "variant_b" else "control"
    }

    override fun getAllFlags(): Map<String, FlagValue> = mapOf(
        "checkout_experiment" to FlagValue.of(
            variant,
            metadata = mapOf("experiment" to "checkout_v2"),
        ),
    )
}

class FlagLensSampleApp : Application() {
    internal val experimentsProvider = ExampleExperimentsProvider()

    override fun onCreate() {
        super.onCreate()

        FlagLens.initialize(
            context = this,
            config = FlagLensConfig(
                enabled = BuildConfig.DEBUG,
                appName = "FlagLens Sample",
                environment = "staging",
                allowLocalOverrides = BuildConfig.DEBUG,
            ),
        )

        FlagLens.registerFlag(
            key = "new_checkout",
            value = true,
            source = "firebase_remote_config",
            metadata = mapOf("experiment" to "checkout_v2", "variant" to "control"),
        )
        FlagLens.registerFlag(key = "dark_mode_default", value = false, source = "static_config")
        FlagLens.registerFlag(key = "api_key", value = "sk_live_example_not_real", source = "static_config")

        FlagLens.registerProvider("experiments", experimentsProvider)

        FlagLens.setContext(key = "user_segment", value = "trial_user")
        FlagLens.setContext(key = "api_environment", value = "staging")
        FlagLens.setContext(key = "build_variant", value = if (BuildConfig.DEBUG) "debug" else "release")
    }
}
