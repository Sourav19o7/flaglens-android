# Publishing

## Current status

FlagLens `0.1.0` is **not published** to any Maven repository. Use it today via a Gradle included
build or by copying the `:flaglens` module directly.

## Generate a local AAR

```bash
./gradlew :flaglens:assembleRelease
# output: flaglens/build/outputs/aar/flaglens-release.aar
```

## Publish to Maven Local

Add to `flaglens/build.gradle.kts`:

```kotlin
plugins {
    // ...existing plugins
    id("maven-publish")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "dev.local.androidtools"
            artifactId = "flaglens"
            version = "0.1.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}
```

then:

```bash
./gradlew :flaglens:publishToMavenLocal
```

## Future: GitHub Packages / Maven Central

Same story as ReproKit and OfflineLab: needs a signing key and/or a `write:packages` token, both
supplied only via GitHub Actions repository secrets — **never** committed to this repo.
`.gitignore` already excludes `local.properties`, `*.jks`, `*.keystore`, and `keystore.properties`.
