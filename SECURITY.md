# Security Policy

## Supported versions

FlagLens is pre-1.0 (`0.1.0`). Only the latest published version is supported.

## Reporting a vulnerability

Please **do not** open a public GitHub issue for security-sensitive findings (e.g. a masking
bypass, or a way for local overrides to become active in a release build). Open a
[private security advisory](../../security/advisories/new) instead, or contact the maintainer
directly.

## Threat model

FlagLens is a **local-only, debug-time** tool that displays whatever the host app registers with
it — it has no network access and does not fetch flags itself. Its threat model assumes:

- The host app wires `FlagLensConfig.enabled` to its own debug flag; FlagLens cannot verify this.
- The panel may be shown on a shared or borrowed device during QA — this is why masking of
  sensitive-looking keys defaults to **on**, and why local overrides require two independent
  opt-ins (`enabled` **and** `allowLocalOverrides`) rather than one.
- Anything a host app explicitly registers via `registerFlag`/`registerProvider`/`setContext` is
  assumed to be something the developer intended to be inspectable in a debug panel. FlagLens's
  masking is a safety net for accidentally-registered secrets, not a guarantee against a developer
  deliberately registering one under a non-obvious key name.

## What would count as a security bug here

- A key matching the default sensitive-key list (`token`, `password`, `secret`, `api_key`, etc.,
  in any separator style) that is **not** masked when `maskingEnabled = true`.
- `FlagLens.setOverride` succeeding when `enabled == false`, regardless of `allowLocalOverrides`.
- `FlagLens.exportJson`/`exportMarkdown` including an unmasked sensitive value that the panel
  itself displays masked.
- Any network call originating from this library. FlagLens has none — finding one would be a
  critical bug.
