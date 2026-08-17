$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Stopping Gradle/Java processes..." -ForegroundColor Cyan
try { .\gradlew.bat --stop | Out-Host } catch {}
Get-Process java, javaw -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "Removing project-local build state only..." -ForegroundColor Cyan
@(".gradle", "build", "app\build", "buildSrc\.gradle", "buildSrc\build", "codeview\build", "hidden-api-stubs\build", "hidden-api-impl\build", "hidden-api-refined\build") |
    ForEach-Object { Remove-Item -Recurse -Force $_ -ErrorAction SilentlyContinue }

Write-Host "Building with the normal Gradle user path and Gradle 8.9..." -ForegroundColor Cyan
Remove-Item Env:GRADLE_USER_HOME -ErrorAction SilentlyContinue
.\gradlew.bat --no-daemon --no-watch-fs --refresh-dependencies clean :app:assembleRootFdroidRelease

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Build finished successfully." -ForegroundColor Green
Write-Host "APK: app\build\outputs\apk\root\fdroid\release\" -ForegroundColor Green
