<h1 align="center">
  <img alt="Icon" width="75" src="https://github.com/thepbone/RootlessJamesDSP/blob/master/img/icons/web/icon-192.png?raw=true">
  <br>
  RetroidJamesDSP
  <br>
</h1>

<h4 align="center">System-wide JamesDSP audio processing optimized for Retroid Android handhelds</h4>

<p align="center">
  <img alt="Version" src="https://img.shields.io/badge/version-1.0.0--rj1.6.14-blue">
  <img alt="Android" src="https://img.shields.io/badge/platform-Android-green">
  <img alt="License" src="https://img.shields.io/github/license/Andiweli/RetroidJamesDSP">
</p>

## About

**RetroidJamesDSP** is a Retroid-specific version of [RootlessJamesDSP](https://github.com/timschneeb/RootlessJamesDSP), providing system-wide [JamesDSP](https://github.com/james34602/JamesDSPManager) audio processing on supported Retroid handhelds.

It combines the JamesDSP processing engine with a Retroid-specific root audio configuration and speaker presets.

Based on **RootlessJamesDSP 1.6.14**.

## Supported devices & presets

Retroid-specific profiles are included for:

- **Retroid Pocket 5**
- **Retroid Pocket 6**
- **Retroid Pocket Flip 2**
  - Normal
  - Quiet
  - Sparkle

Other Retroid devices may work, but are currently not specifically tuned.

<img width="1920" height="1080" alt="image0" src="https://github.com/user-attachments/assets/baf49fcd-ee28-4840-933c-59adfbc19790" />


## Retroid integration

Compared with standard RootlessJamesDSP, this version adds:

- Retroid-specific system audio integration
- System-wide JamesDSP processing through the root audio path
- Automatic DSP initialization after reboot
- AudioPolicy restart recovery
- Automatic legacy audio-session recovery
- Media-session warm-up for reliable DSP activation
- One-click Retroid speaker presets
- Retroid-specific setup screen

The original rootless / MediaProjection mode is not used for the Retroid integration.

## Installation

1. Download and install the latest APK from **[Releases](https://github.com/Andiweli/RetroidJamesDSP/releases)**.
2. Open **RetroidJamesDSP**.
3. Run the Retroid audio setup.
4. Reboot the device.
5. Open RetroidJamesDSP and select the desired Retroid preset.

A reboot is recommended after installing or updating the application.

> **Important:** RetroidJamesDSP modifies the device's audio configuration and requires the supported Retroid root / temporary-root environment.

## Building

Build the Retroid release variant with:

```bash
./gradlew clean :app:assembleRootFdroidRelease
```

The APK is created under:

```text
app/build/outputs/apk/root/fdroid/release/
```

See [BUILDING.md](BUILDING.md) for additional build information.

## Credits

RetroidJamesDSP builds upon several open-source projects:

- **[RootlessJamesDSP](https://github.com/timschneeb/RootlessJamesDSP)** — Tim Schneeberger
- **[JamesDSP / libjamesdsp](https://github.com/james34602/JamesDSPManager)** — James Fung
- **[jdsp4rp5.app](https://github.com/kokoko3k/jdsp4rp5.app)** — kokoko3k

Parts of the Retroid integration are based on work from `jdsp4rp5.app`. Its author has explicitly granted permission for those contributions to be used under GPL-3.0 in this project.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for details.

## ❤️ Support

If you enjoy this project and would like to support my work, you can make a small contribution via PayPal.

Your support helps me spend more time maintaining existing projects, fixing bugs, improving compatibility, and working on new features.

[![Support via PayPal](https://img.shields.io/badge/Support%20via-PayPal-0070BA?logo=paypal\&logoColor=white)](https://paypal.me/andiweli)

Thank you for your support!

## License

RetroidJamesDSP is distributed under the **GNU General Public License v3.0**.

See [LICENSE](LICENSE) for details.

---

**RetroidJamesDSP is an unofficial community project and is not affiliated with or endorsed by Retroid, James Fung, or the RootlessJamesDSP project.**
