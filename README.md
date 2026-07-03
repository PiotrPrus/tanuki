# Tanuki

An open-source, Compose Multiplatform (Android + iOS) client for GitLab — think
"GitHub Mobile, but for GitLab". Browse your projects, review merge requests, and act on
them from your phone. Community-driven and MIT-licensed.

> Not affiliated with or endorsed by GitLab Inc.

## Vision

A polished, general-purpose GitLab client that works for **anyone** on gitlab.com (with
self-hosted instances on the roadmap), not a single-company tool. Contributions welcome.

## Status: early scaffold

Both platforms compile today:
- Android: `./gradlew :composeApp:assembleDebug` → APK
- iOS framework: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`

**v1 target:** OAuth login → Projects browser (list/search/star) → Merge requests
(review-requested / assigned, detail, approve, comment).
**Backlog:** Issues, repository/code browser, pipelines/CI, to-dos, self-hosted instances,
snippets.

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
:feature:mergerequests:{domain,data,presentation}    MR list/detail (WIP)
:feature:projects:{domain,data,presentation}          projects browser (planned)
:build-logic                                         KMP convention plugins (tanuki.kmp.*)
```

- Gradle 8.14.3, AGP 8.11.2, Kotlin 2.3.0, Compose Multiplatform 1.10.0
- DI: Koin · Networking: Ktor · Nav: type-safe Compose Navigation · Secure storage:
  multiplatform-settings (Keychain / EncryptedSharedPreferences)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Issues and PRs welcome — good first issues will be
labelled once the repo is public.

## License

[MIT](LICENSE) © 2026 Piotr Prus and Tanuki contributors
