@echo off
chcp 65001 >nul
echo.
echo ============================================
echo   VARAQ Android - bir martalik Gradle sozlash
 echo ============================================
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0SETUP_WINDOWS.ps1"
if errorlevel 1 (
    echo.
    echo XATOLIK: Internet aloqasi yoki PowerShellni tekshiring.
) else (
    echo.
    echo Tayyor! Endi Android Studio orqali shu papkani oching.
)
pause
