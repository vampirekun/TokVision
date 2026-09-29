# TokVision 1.0 Release Gate

This checklist is the single pre-release gate for TokVision 1.0. A release is ready only when every **Required** gate is marked PASS and the evidence is linked or reproducible from the repository.

## Gate criteria

| Area | Required | Pass condition | Evidence |
|---|---|---|---|
| Functional | Yes | Login/logout and every feature included in the release scope work on the supported Android TV target; no known release-blocking defect remains. | Test result / issue links |
| Security | Yes | No known release-blocking security issue; OAuth flow uses the documented official TikTok APIs; no secrets are committed. | CI/security review |
| Privacy / legal | Yes | Privacy/data-retention behavior is documented and no unofficial TikTok endpoint or scraping path is enabled. | `KNOWN_LIMITATIONS.md` + review |
| Performance | Yes | Debug/release build completes and no release-blocking startup, memory, CPU or playback regression is known on the supported target. | Test notes / benchmark |
| Accessibility | Yes | D-pad navigation reaches all release-scope controls, focus is visible, and important actions have usable labels. | Manual QA notes |
| Compatibility | Yes | Release build installs and launches on the declared Android TV / Google TV support range. | Device/OS matrix |
| Documentation | Yes | README, BUILD instructions and known limitations describe the actual release behavior and required setup. | Documentation review |
| Landing page / product docs | Yes | Public-facing project description and screenshots/features match the released scope; no promised feature is presented as implemented when it is not. | Review |
| Distribution | Yes | The intended distribution channel and installation artifact are identified and the release artifact is reproducible from the tagged commit. | Release checklist |
| Donation / support | No | If support/donation is enabled, links work and do not block installation or use. If not enabled, mark N/A. | Link check |
| Signed artifacts | Yes for production distribution | Release APK is signed with the intended production key and the signing process does not expose credentials in Git. | Release evidence; never commit keys |

## Release decision

Mark the release **READY** only when:

- Every Required gate is **PASS**.
- Every N/A decision is explicitly justified.
- All release-blocking issues are closed or explicitly deferred outside the 1.0 scope.
- The release artifact was built from the exact commit being tagged.
- No credentials, private keys or `secrets.properties` contents are included in the repository or artifact.

## Evidence rule

Evidence should be concrete: CI run, test result, device/OS combination, file path, release artifact, or issue/PR reference. Avoid replacing evidence with statements such as "looks good".

## Known-scope rule

TokVision must not claim support for functionality that the official TikTok APIs do not provide. In particular, the release gate must continue to respect the limitations documented in `KNOWN_LIMITATIONS.md` (for example, third-party "For You" feeds, search, likes/follows, and comments).

## Sign-off

Before tagging 1.0:

- [ ] Required gates reviewed
- [ ] Evidence attached or reproducible
- [ ] Release scope frozen
- [ ] Release artifact built from release commit
- [ ] Tag created only after the gate passes
