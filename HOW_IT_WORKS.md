# How FlagLens works

The learning-oriented companion to the README. If you've read ReproKit's or OfflineLab's
`HOW_IT_WORKS.md`, some of this will feel familiar — this project family deliberately reuses the
same handful of design patterns (singleton facade over swappable state, key-name masking, pure
internal logic tested without Android) rather than inventing a new approach per tool, which is
itself worth noticing as a lesson: once a pattern works, repeating it deliberately is a feature,
not a lack of creativity.

## Why flags are "push" (`registerFlag`) and "pull" (`FlagProvider`) at the same time

A naive design would pick one model. But real apps have both kinds of flag sources: a static
build-time map (push it once, it never changes) and a live remote-config SDK (its value can change
at any moment, and re-reading it late is exactly as correct as re-reading it early). Modeling both
with a single `Map<String, FlagValue>` snapshot taken once at `registerFlag`-time would make the
remote-config case wrong the moment the SDK's underlying value changes — the panel would show
stale data with no way to know it's stale.

`FlagRegistry.snapshot()` (`internal/FlagRegistry.kt`) handles this by treating the two paths
differently:

```kotlin
for (pushed in pushedFlags.values) { entries += toFlag(...) }          // stored once
for ((providerName, provider) in providers) {
    for ((key, value) in provider.getAllFlags()) { entries += toFlag(...) } // queried fresh, every time
}
```

`pushedFlags` is a map you write into once (or repeatedly, if you want) — cheap, simple, right for
values that don't drift on their own. `providers` is a map of *sources*, each queried with a fresh
`getAllFlags()` call on every single `snapshot()` — more expensive per call, but the only way to
guarantee correctness for a live SDK. `FlagRegistryTest`'s "provider flags are queried live on
every snapshot" test proves this directly: it mutates a captured variable between two `snapshot()`
calls and asserts the second call sees the new value, without ever calling `registerProvider`
again.

## Why `FlagProvider` is a `fun interface`

```kotlin
fun interface FlagProvider {
    fun getAllFlags(): Map<String, FlagValue>
}
```

A single-abstract-method interface gets Kotlin's SAM (single abstract method) conversion for
free: `FlagLens.registerProvider("experiments") { mapOf(...) }` works with a trailing lambda, no
anonymous `object : FlagProvider { ... }` boilerplate needed for the common case, while still
supporting a full class implementation (like the sample app's `ExampleExperimentsProvider`, which
needs mutable internal state a lambda can't hold cleanly). This is the same reasoning behind
OfflineLab's `RandomSource` being a `fun interface`.

## Why overrides need *two* independent booleans, not one

`FlagLensConfig` has both `enabled` and `allowLocalOverrides`, and `FlagLens.setOverride` checks
them in a specific order:

```kotlin
fun overridesAllowed(): Boolean = isEnabled() && config?.allowLocalOverrides == true

fun setOverride(key: String, value: String): Boolean {
    if (!overridesAllowed()) return false
    ...
}
```

A single `allowOverrides` flag would technically work, but it creates exactly one accidental
failure mode this design is built to avoid: someone flips `allowOverrides = true` in a shared
config object for local testing, forgets to flip it back, and it ships to production still `true`
— now overrides are reachable in a release build if `enabled` were ever miscomputed too. Requiring
**both** `enabled` (already tied to `BuildConfig.DEBUG` by convention across this whole project
family) and a *second*, override-specific flag means a single mistake in one flag isn't enough —
someone would have to deliberately misconfigure two independent things for overrides to leak into
production. This is the concrete mechanism behind the README's "impossible to accidentally enable"
claim; it's not a policy statement, it's this `&&`.

## Why `Flag` keeps `actualValue` and `overrideValue` as separate fields, not one merged value

```kotlin
data class Flag(
    val actualValue: FlagValue,
    val isOverridden: Boolean = false,
    val overrideValue: String? = null,
    ...
) {
    val effectiveValue: String get() = if (isOverridden) overrideValue.orEmpty() else displayValue
}
```

If `Flag` only stored one "current" value, a developer looking at the panel three days after
setting an override could easily forget they set it and mistake the overridden value for the real
one — exactly the kind of confusing state a *debugging* tool should never introduce. Keeping both
fields, and rendering `real → override` in the panel whenever `isOverridden` is true (see
`FlagLensPanel.kt`'s `FlagRow`), makes the override impossible to miss. `effectiveValue` is
provided as a convenience for "what would the app actually see," but it's derived, never the only
source of truth stored.

## Why masking happens inside `FlagRegistry`/`ContextStore`, not in the UI layer

`Masker.isSensitive(key)` is invoked when building a `Flag`/`ContextEntry` (in
`FlagRegistry.toFlag()` and `ContextStore.set()`), not when the Compose panel renders. This means
`Flag.displayValue` is *already* masked by the time it reaches `FlagReportSerializer` for
export — there's no separate "mask before export" step that could be forgotten. A design that
masked only at render time would have to remember to apply the same masking logic again in the
export path, and "remember to apply it again in N places" is exactly the kind of thing that quietly
stops being true after a refactor six months later. Doing it once, at data-construction time,
means every consumer of a `Flag`/`ContextEntry` — panel, JSON export, Markdown export, any future
consumer — gets masking for free and can't opt out by accident.

## Why the library module itself depends on Compose (unlike ReproKit/OfflineLab)

ReproKit's and OfflineLab's *libraries* have zero Compose dependency — only their sample apps do.
FlagLens's `:flaglens` module depends on Compose directly, because `FlagLensPanel()` is a public
API meant to be embedded in a host app's own Compose navigation (see README's Compose usage
section) — it can't be "only in the sample app" and still be usable that way. The trade-off,
disclosed in Known limitations: this makes FlagLens Compose-only in `0.1.0` (no classic View
entry point), whereas a tool whose UI is optional (like ReproKit's exported Markdown, rendered by
whatever UI the host chooses) doesn't need to make that choice.

## Why `FlagLens`'s facade has no unit tests, while `FlagRegistry`/`Masker`/`FlagQuery` do

Every method on the `FlagLens` object that needs Android state goes through `initialize(context,
config)`, which requires a real `android.content.Context`. Plain JUnit tests (no Robolectric, no
instrumentation — see the "why no Robolectric" reasoning in ReproKit's `HOW_IT_WORKS.md`, which
applies identically here) can't easily construct one. So, deliberately: all the *logic* that
doesn't need a `Context` — merging providers, masking, searching, grouping, serializing — lives in
`internal/` classes that take plain Kotlin values as constructor parameters (`Masker(enabled,
keys)`, `FlagRegistry(masker, maxAuditEntries)`) and are fully unit tested. The thin facade that
*does* need a `Context` is exercised manually via the sample app instead. This is the same "push
framework dependencies to the edges" split used throughout this project family, and the honest
disclosure of it (rather than silently having zero facade coverage and not mentioning it) is in
the README's Known limitations and this project's `CHANGELOG.md`.
