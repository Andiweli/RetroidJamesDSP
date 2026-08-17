$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
.\gradlew.bat clean :app:assembleRootFdroidRelease
