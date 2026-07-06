# Changelog

All notable changes to Tanuki are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1] — 2026-07-06

### Added
- **Merge widget** on the merge-request screen: head-pipeline checks and merge status, with
  contextual **Rebase** and **Merge** actions. (#35)
- **Code-review conversations**: the commented code in context, threaded replies, resolve/reopen,
  and Markdown-rendered comments (blockquotes, lists, inline code). (#36)
- **Group browser**: drill through groups and subgroups, reached by tapping a segment in a
  project's breadcrumb. (#34)
- **Star / unstar** repositories directly from the projects list and the group browser. (#34)

### Changed
- **Projects** is now a tabbed repository list — **Recent · Starred · Personal · Member** — with a
  filter field; group browsing lives on breadcrumbs. (#37)
- Trimmed the README down to the app itself (features, screenshots). (#33)

## [0.1.0] — 2026-07-06

### Added
- OAuth 2.0 (PKCE) sign-in through the system browser — 2FA and SSO handled by GitLab.
- Projects browser and project dashboard (merge requests, code, tags, releases, pipelines,
  branches, activity).
- Merge requests: review-requested and assigned queues; detail view with reviewers, approvals,
  additions/deletions/files, and tabs for Overview / Commits / Pipelines / Changes.
- Diff viewer with horizontally-scrollable code and line-level comments.
- Deep links: shared `gitlab.com/…/-/merge_requests/N` links open in the app (Android).
- Dark and light themes that follow the system setting.
- Android internal distribution via Firebase App Distribution, driven by release tags.
- iOS build running on device.

[0.1.1]: https://github.com/PiotrPrus/tanuki/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/PiotrPrus/tanuki/releases/tag/v0.1.0
