# TokVision Versioning Policy

TokVision uses Semantic Versioning for the public product version:

`MAJOR.MINOR.PATCH`

## Version meanings

- **MAJOR** — incompatible product behavior or a breaking change to a documented public contract.
- **MINOR** — backward-compatible user-facing functionality or a meaningful new feature.
- **PATCH** — backward-compatible bug fixes, maintenance, documentation, and small reliability improvements.

## Android version fields

- `versionName` is the human-readable SemVer value shown to users.
- `versionCode` is an Android monotonically increasing integer and must increase for every published artifact.
- A versionCode change without a versionName change is allowed for rebuilds only when Android distribution requires a new artifact; otherwise prefer a patch release.

## Release flow

1. Changes land in the default development branch through reviewed pull requests.
2. Release scope is frozen before a release candidate.
3. CI must pass for the release candidate.
4. The release version is recorded in Gradle configuration.
5. A Git tag is created using the `vMAJOR.MINOR.PATCH` format.
6. Release notes describe user-visible changes, known limitations, and validation performed.
7. Release artifacts are built from the tagged commit.

## Pre-1.0 policy

While TokVision remains below 1.0, minor versions may contain larger changes than they would after 1.0. Breaking changes should still be called out explicitly in release notes.

## Stable 1.0 exit criteria

TokVision should not be declared 1.0 until the project has a verified authentication flow, documented API limitations, passing automated tests, release/build validation, privacy/security documentation, and a repeatable release process.

## Changelog expectations

Each release should include:

- Added
- Changed
- Fixed
- Security/privacy notes when applicable
- Known limitations when applicable

Do not include secrets, access tokens, private URLs, or personal device information in release notes.
