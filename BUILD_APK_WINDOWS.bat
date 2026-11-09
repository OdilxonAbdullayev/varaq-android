@echo off
setlocal
cd /d "%~dp0"
echo [VARAQ] Gradle sozlanmoqda...
call "%~dp0SETUP_WINDOWS.bat"
if errorlevel 1 (
  echo XATO: Gradle sozlanmadi.
  pause
  exit /b 1
)
if not exist "%~dp0gradlew.bat" (
  echo XATO: gradlew.bat topilmadi.
  pause
  exit /b 1
)
echo [VARAQ] Android APK yig'ilmoqda...
call "%~dp0gradlew.bat" --no-daemon assembleDebug
if errorlevel 1 (
  echo XATO: APK yig'ish muvaffaqiyatsiz. Android SDK va internetni tekshiring.
  pause
  exit /b 1
)
echo.
echo TAYYOR: app\build\outputs\apk\debug\app-debug.apk
explorer "%~dp0app\build\outputs\apk\debug"
pause
