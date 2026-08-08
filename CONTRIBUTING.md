# Contributing to FlagLens

## Setup

```bash
git clone https://github.com/Sourav19o7/flaglens-android.git
cd flaglens-android
./gradlew :flaglens:testDebugUnitTest
```

Open in Android Studio (Hedgehog+); `:flaglens` (library) and `:sample` (Compose demo) both show up.

## Before opening a PR

```bash
./gradlew :flaglens:testDebugUnitTest
./gradlew :flaglens:lintDebug
./gradlew :flaglens:assembleDebug :sample:assembleDebug
```

## Code style

- `.editorconfig` in the repo root defines formatting.
- Public API lives in `dev.local.androidtools.flaglens` and `.ui`; everything else belongs under
  `internal/`.
- `FlagLensPanel()` (in `ui/`) is public and Compose-only in `0.1.0` — see README's Known
  limitations for the missing classic-View entry point.

## Adding a new sensitive key to the default mask list

Edit `internal/Masker.kt`'s `DEFAULT_SENSITIVE_KEYS`, add a `MaskerTest` case covering at least one
separator-style variant (e.g. `X-New-Key` as well as `new_key`), and mention it in the README's
Security and privacy section.

## Adding a new `FlagProvider` example

Provider implementations belong in the consuming app, not in this library (see README's Firebase
adapter section for why) — contribute example code to the README or sample app, not a new library
module, unless there's a strong reason FlagLens itself needs to own it.

## Reporting bugs / requesting features

Use the issue templates. Anything touching masking or override gating gets extra review — see
`SECURITY.md`.
