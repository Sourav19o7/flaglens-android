# Changelog

All notable changes to this project are documented in this file. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versioning follows
[SemVer](https://semver.org/).

## [0.1.0] - Unreleased

Initial MVP.

### Added

- `FlagLens.initialize` / `FlagLensConfig`, with local overrides gated behind two independent
  flags (`enabled` and `allowLocalOverrides`).
- `FlagProvider` interface (pull-based) plus `registerFlag` (push-based) and `StaticMapFlagProvider`.
- Runtime context (`setContext`) separate from flags.
- `FlagLensPanel()` Compose component and `FlagLensActivity` host, opened via `FlagLens.show()`.
- Search, grouping by source, local overrides with a session audit trail, and sensitive-key
  masking (same normalized key-matching approach as ReproKit's redactor).
- Markdown/JSON export and clipboard copy.
- Compose sample app: sample flags, a live experiment provider, embedded-panel and
  launched-Activity panel usage, and an override demo.

### Known limitations

- No shake-gesture or hidden-tap-sequence activation — only explicit (`FlagLens.show()` /
  `FlagLensPanel()`) and programmatic invocation are implemented in `0.1.0`. The spec's other
  suggested entry points are documented as future work, not implemented — see README Roadmap.
- No classic View-system entry point (Compose only).
- No compiled Firebase Remote Config adapter (by design — see README's Firebase adapter section
  for an example you can copy into your own app; this library never depends on Firebase).
- `FlagLens`'s facade layer (the singleton itself) has no automated test coverage — it requires an
  Android `Context` to initialize. Its internals (`FlagRegistry`, `Masker`, `FlagQuery`,
  `FlagReportSerializer`) are fully unit tested; the facade is exercised manually via the sample
  app, same pattern as ReproKit/OfflineLab's facades.
