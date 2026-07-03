# Contributing to Tanuki

Thanks for your interest! Tanuki is a community GitLab client. This guide covers how the
codebase is organised and what we expect from contributions.

## Getting started

- Install Android Studio (with the Kotlin Multiplatform plugin) and Xcode (for iOS).
- JDK 17+ recommended.
- Build checks:
  - `./gradlew :composeApp:assembleDebug` — Android
  - `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` — iOS framework
  - `./gradlew test` — unit tests (as they land)

## Architecture

Feature-layered clean architecture on Kotlin Multiplatform:

- Split by **feature** first, then by **layer** (`domain` / `data` / `presentation`).
- Dependency direction: `presentation → domain ← data`. `domain` depends on nothing but
  `:core:domain`.
- **Features never depend on other features.** Shared code moves to the relevant `:core:*`
  module.
- Presentation uses MVI (State / Action / Event + a `Root` composable that owns the
  ViewModel and a stateless screen composable).
- Each module applies a convention plugin from `:build-logic` (`tanuki.kmp.library`,
  `tanuki.kmp.compose`, `tanuki.kmp.feature`, `tanuki.kmp.application`). Versions live in
  `gradle/libs.versions.toml` — no hardcoded versions in build files.

## Adding a feature

1. Create `:feature:<name>:{domain,data,presentation}` and register them in
   `settings.gradle.kts`.
2. Apply the right convention plugin to each layer; set the module `namespace`.
3. Wire Koin modules and add them to `initKoin()` in `:composeApp`.
4. Add a type-safe route in `:composeApp` navigation.

## Pull requests

- Keep PRs focused; one feature/fix per PR.
- Match the surrounding style; run `./gradlew :composeApp:assembleDebug` before pushing.
- Describe what you changed and how you verified it.
- By contributing you agree your work is licensed under the project's [MIT License](LICENSE).

## Auth / secrets

Never commit a real OAuth Application ID, token, or `local.properties`. The `CLIENT_ID` in
`GitLabConfig` is a placeholder each developer fills in locally.
