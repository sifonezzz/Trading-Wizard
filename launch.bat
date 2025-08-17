@echo off
:: ============================================================================
::  Trading Wizard Launcher (v2 - with Debug Mode)
::  This script starts the JavaFX application. By default, it keeps the
::  window open to display any startup errors.
:: ============================================================================

TITLE Trading Wizard Launcher

:: --- PRE-FLIGHT CHECKS ---

:: 1. Go to the script's directory to ensure paths are correct.
cd /d "%~dp0"

:: 2. Check for JAVA_HOME - Maven often relies on this.
if not defined JAVA_HOME (
    echo [ERROR] The JAVA_HOME environment variable is not set.
    echo Please make sure you have a JDK (version 17+) installed and JAVA_HOME is configured.
    echo.
    pause
    exit /b
)

:: 3. Check for the Java command in the system PATH.
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] The 'java' command was not found in your PATH.
    echo Please make sure the JDK's 'bin' directory is in your system's PATH environment variable.
    echo.
    pause
    exit /b
)

:: 4. Check for the Maven command in the system PATH.
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] The 'mvn' command was not found in your PATH.
    echo Please make sure Apache Maven's 'bin' directory is in your system's PATH environment variable.
    echo.
    pause
    exit /b
)


:: --- LAUNCH ---

echo --- DEBUG MODE ---
echo This window will stay open to show application output and any errors.
echo Launching Trading Wizard... This may take a moment.
echo.

:: This command runs the app directly. If there are any errors, you will see them here.
call mvn javafx:run

echo.
echo Application has been closed.
pause


:: --- FINAL LAUNCH MODE (Use this after debugging) ---
:: Once you confirm the command above works, you can enable this mode.
:: To enable, remove the "REM" from the two lines below and add "REM" to the
:: "call mvn javafx:run" and "pause" lines above.

REM echo Launching Trading Wizard...
REM START "Trading Wizard" /B mvn javafx:run