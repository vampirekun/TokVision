# Contributing to TokVision

Thanks for helping improve TokVision, an Android TV client. Changes should preserve remote-control navigation, protect account tokens, and be honest about what can and cannot be tested without TikTok credentials or a physical TV.

## Before opening an issue or pull request

1. Search existing issues and pull requests to avoid duplicates.
2. Describe one problem or capability per change.
3. Never include TikTok client secrets, access/refresh tokens, personal account data, or unredacted device logs.
4. For security vulnerabilities, follow [SECURITY.md](SECURITY.md); do not report an unpatched vulnerability publicly.

## Local setup

Requirements: JDK 17 and the Gradle wrapper included in this repository. Android Studio is optional but useful for emulator/device testing.

On macOS/Linux:

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat lintDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

The same lint, unit-test, and debug-build tasks run in GitHub Actions. A successful build does not prove that TikTok OAuth works: real authentication requires a correctly registered developer app and local values in `secrets.properties`. Never commit that file or real credentials. The app should remain buildable with the documented placeholder values.

## Android TV and remote-control expectations

- Every interactive control must be reachable and understandable with a D-pad/remote; do not rely on touch-only gestures.
- Keep focus visible and predictable when lists, dialogs, loading states, or errors change.
- Handle Back consistently and avoid trapping focus in overlays.
- Include device model, Android version, app version/commit, and reproduction steps when reporting a device-specific issue.
- Distinguish emulator results from physical Android TV results.

## Pull request checklist

- [ ] Explain the user problem and the bounded scope.
- [ ] Add or update unit tests for changed logic.
- [ ] Run lint, unit tests, and debug build; report anything that could not be run.
- [ ] Check D-pad focus, Back navigation, and empty/loading/error states when relevant.
- [ ] Update documentation for changed setup or behavior.
- [ ] Remove secrets, personal data, generated artifacts, and unrelated edits.
- [ ] Do not claim physical-device or real OAuth validation unless it was actually performed.

Keep pull requests focused and reviewable. Maintainers may request changes when behavior, compatibility, or test evidence is unclear.
