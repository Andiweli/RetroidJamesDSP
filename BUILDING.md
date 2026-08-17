# Building RetroidJamesDSP

## Supported release variant

RetroidJamesDSP is built from the **root + F-Droid** flavor:

- Android Studio variant: `rootFdroidRelease`
- Gradle task: `:app:assembleRootFdroidRelease`

Windows PowerShell:

```powershell
.\gradlew.bat clean :app:assembleRootFdroidRelease
```

A convenience script is also included:

```powershell
.\build-retroid-root-release.ps1
```

The APK output is written below:

`app/build/outputs/apk/root/fdroid/release/`

## Toolchain

- JDK 17
- Android SDK / Build Tools compatible with compileSdk 35
- Gradle wrapper included with the repository

## Important

Do not use the `rootless` or `plugin` flavors for the Retroid one-app build.

The build scripts can read Git commit metadata when the repository has a `.git`
directory. Exported source archives remain buildable because safe fallback values
are used when Git metadata is unavailable.
