# Changelog

All notable changes to RetroidJamesDSP will be documented in this file.

The project version is independent from the upstream RootlessJamesDSP version. The
upstream base used for each RetroidJamesDSP release is recorded explicitly.

## 1.0.0-rj1.6.14 - Initial public release

**Upstream base:** RootlessJamesDSP 1.6.14

### Retroid integration
- Integrated Retroid temporary-root audio setup into the JamesDSP root build.
- Added a compact Retroid-oriented setup screen and one-app workflow.
- Uses the root/global legacy audio-effect session instead of the rootless MediaProjection path.
- Added recovery after Retroid audio-policy / AudioFlinger restarts.
- Added a delayed single legacy-session reopen to avoid detached-engine failures.
- Added a silent MEDIA warm-up after the audio-policy restart so applications can attach to the restored global effect chain reliably.
- Prevented the Retroid setup flow from launching the rootless engine path.
- Added boot-time initialization support for the Retroid audio patch.

### Retroid presets
- Retroid Pocket 5
- Retroid Pocket 6
- Flip 2 Normal
- Flip 2 Quiet
- Flip 2 Sparkle

### Stability and build fixes
- Added DSP preference/profile guarding for the active Retroid preset.
- Added safe Git metadata fallbacks so exported source archives can build without a `.git` directory.
- Standardized the supported release variant on `rootFdroidRelease`.
- Disabled the upstream RootlessJamesDSP self-update service and removed its Play/update entries from the Retroid root About screen.

### Notes
- After a fresh installation or update of the Retroid audio integration, a device reboot is recommended before final audio testing.
- RetroidJamesDSP is an unofficial community project and is not affiliated with or endorsed by Retroid.
