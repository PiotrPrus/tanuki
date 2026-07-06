# Tanuki

An open-source, Compose Multiplatform (Android + iOS) client for GitLab — think
"GitHub Mobile, but for GitLab". Browse your projects, review merge requests, and act on
them from your phone. Community-driven and MIT-licensed.

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS-brightgreen.svg)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg)

> Not affiliated with or endorsed by GitLab Inc.

## Screenshots

| Projects | Project dashboard | Merge request | Diff viewer |
|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/projects.png" width="200"/> | <img src="docs/screenshots/dashboard.png" width="200"/> | <img src="docs/screenshots/mr-detail.png" width="200"/> | <img src="docs/screenshots/mr-changes.png" width="200"/> |

*Dark theme shown; Tanuki follows the system light/dark setting.*

## Vision

A polished, general-purpose GitLab client that works for **anyone** on gitlab.com (with
self-hosted instances on the roadmap), not a single-company tool. Contributions welcome.

## Features

- **OAuth 2.0 + PKCE login** through the system browser — 2FA and SSO handled by GitLab's web login.
- **Projects browser** — list, search, and filter (All / Starred / Personal / Shared) with star counts.
- **Project dashboard** — merge requests, code, tags, releases, pipelines, branches, and an activity pulse.
- **Merge requests** — review-requested & assigned queues; a rich detail view with reviewers + approval
  status, additions/deletions/files stats, and tabs for Overview / Commits / Pipelines / Changes.
- **Diff viewer** — horizontally-scrollable code with line-level comments (view, reply, resolve).
- **Deep links** — shared `gitlab.com/…/-/merge_requests/N` links open straight to the MR (Android).
- **Dark & light themes** — follows the system setting.

## Status

Both platforms **build and run on device** today (Android phones and iPhone). The v1 surface —
OAuth login, projects browser, and merge-request review — is implemented.

**Backlog:** issues, full repository/code browser, richer pipelines/CI, to-dos, self-hosted
instances, snippets, and iOS distribution via Firebase.

## Auth model

Login uses **OAuth 2.0 with PKCE** through the system browser (Chrome Custom Tabs on
Android, `ASWebAuthenticationSession` on iOS). This means **2FA and SSO are handled by
GitLab's own web login for free** — the app never sees a password or OTP. This is the same
approach GitHub Mobile uses and the only robust way to support 2FA, since GitLab disables
password-based API grants when 2FA is enabled.

The data layer is built around a configurable instance URL, so self-hosted GitLab support
(via per-instance OAuth apps or Personal Access Tokens) can be added without rework.

## Getting it running locally

1. Register an OAuth application on gitlab.com
   (https://gitlab.com/-/user_settings/applications): **uncheck "Confidential"** (public
   PKCE client), redirect URI `dev.tanuki://oauth-callback`, scope `api`.
2. Paste the Application ID into `core/data/.../network/GitLabConfig.kt` (`CLIENT_ID`).
3. Android: open in Android Studio or `./gradlew :composeApp:installDebug`.
   iOS: open `iosApp/iosApp.xcodeproj`, set your signing Team in
   `iosApp/Configuration/Config.xcconfig`, and run.

## Architecture

Kotlin Multiplatform, feature-layered clean architecture. See `CONTRIBUTING.md`.

```
:composeApp                                          app entry, nav host, Koin startup, platform entry points
:core:domain | :core:data | :core:presentation | :core:design-system
:feature:auth:{domain,data,presentation}             OAuth PKCE, login
:feature:mergerequests:{domain,data,presentation}    MR list, detail, review, diff comments
:feature:projects:{domain,data,presentation}         projects browser, dashboard, code/tags/releases/pipelines
:build-logic                                         KMP convention plugins (tanuki.kmp.*)
```

- Gradle 8.14.3, AGP 8.11.2, Kotlin 2.3.0, Compose Multiplatform 1.10.0
- DI: Koin · Networking: Ktor · Nav: type-safe Compose Navigation · Secure storage:
  multiplatform-settings (Keychain / EncryptedSharedPreferences)

## Versioning

Tanuki follows [SemVer](https://semver.org). The current version is **0.1.0**.

`versionName` / `versionCode` default from `gradle.properties` (`tanuki.versionName`,
`tanuki.versionCode`) and are overridden in CI from the release tag:

```bash
./gradlew :composeApp:assembleRelease -Ptanuki.versionName=0.1.0 -Ptanuki.versionCode=42
```

## Releases & internal distribution

Distribution to internal testers runs through **Firebase App Distribution**, driven by
release tags. Pushing a SemVer tag builds and ships automatically:

```bash
git tag v0.1.0
git push origin v0.1.0
```

The [`distribute-android`](.github/workflows/distribute-android.yml) workflow then builds a
release APK (versioned from the tag) and uploads it to the `internal-testers` group. It can
also be triggered manually via **workflow_dispatch**.

**Required GitHub secrets:**

| Secret | Purpose |
|---|---|
| `FIREBASE_APP_ID_ANDROID` | Firebase Android app id (`1:…:android:…`) |
| `FIREBASE_SERVICE_ACCOUNT_ANDROID_B64` | base64 of a service-account JSON key with the *Firebase App Distribution Admin* role (the workflow also accepts the raw-JSON `FIREBASE_SERVICE_ACCOUNT_ANDROID` as a fallback) |
| `ANDROID_KEYSTORE_BASE64` | *(optional)* base64 of the release keystore — **omit for now to debug-sign** |
| `ANDROID_KEYSTORE_PASSWORD` / `ANDROID_KEY_ALIAS` / `ANDROID_KEY_PASSWORD` | *(optional)* keystore credentials |

Optional repo **variable** `FIREBASE_TESTER_GROUPS` overrides the default tester group.

> Until a release keystore is configured, builds are **debug-signed** — fine for internal
> testing, but note that switching to a real signing key later changes the app signature, so
> testers will need to uninstall and reinstall.

`composeApp/google-services.json` (Android) and `iosApp/iosApp/GoogleService-Info.plist`
(iOS) hold the Firebase client config. These are safe to commit — they're client
identifiers, not secrets (Firebase relies on security rules + API-key restrictions).

> **iOS distribution** via Firebase is planned. iOS uses Ad Hoc distribution, which needs a
> paid Apple Developer account and each tester's device UDID registered in the provisioning
> profile (with a rebuild when new testers join) — so it's a follow-up.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Issues and PRs welcome — good first issues will be
labelled once the repo is public.

## License

[MIT](LICENSE) © 2026 Piotr Prus and Tanuki contributors
