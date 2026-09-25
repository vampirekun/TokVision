# TokVision Versioning Policy

TokVision uses Semantic Versioning for the public product version:

`MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]`

Examples: `0.2.0-auth`, `0.2.0-beta.1`, `1.0.0-rc.1+45`.

## Version meanings

- **MAJOR** — incompatible product behavior or a breaking change to a documented public contract.
- **MINOR** — backward-compatible user-facing functionality or a meaningful new feature.
- **PATCH** — backward-compatible bug fixes, maintenance, documentation, and small reliability improvements.

## Android version fields

- `versionName` is the human-readable SemVer value shown to users, including prerelease or build metadata when applicable.
- `versionCode` is an Android monotonically increasing integer and must increase for every published artifact.
- A versionCode change without a versionName change is allowed for rebuilds only when Android distribution requires a new artifact; otherwise prefer a patch release.

## Release flow

1. Changes land in the default development branch through reviewed pull requests.
2. Pre-release labels follow SemVer and indicate confidence:
   - `-alpha.N` for early integration work that is not feature-complete or may change substantially.
   - `-beta.N` for feature-complete builds ready for broader testing, with known limitations allowed.
   - `-rc.N` for release candidates that are code-frozen except for release-blocking fixes.
3. Release scope is frozen before a release candidate.
4. CI must pass for beta and release candidate builds.
5. TokVision does not keep long-lived release branches. Releases are normally cut from the default development branch.
6. If stabilization needs isolation from ongoing feature work, create a short-lived `release/MAJOR.MINOR` branch, allow only release-blocking fixes there, and merge it back after tagging.
7. The release version is recorded in Gradle configuration.
8. A Git tag is created using the `vMAJOR.MINOR.PATCH` format for stable releases, or `vMAJOR.MINOR.PATCH-PRERELEASE` for tagged prereleases.
9. Release notes describe user-visible changes, known limitations, and validation performed.
10. Release artifacts are built from the tagged commit.

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
