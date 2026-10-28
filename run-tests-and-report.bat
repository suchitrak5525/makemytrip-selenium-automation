
@echo off
setlocal

cd /d "%~dp0"

:: Create a timestamp for this run
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HH-mm-ss"') do set "RUN_ID=%%i"

set "RUN_DIR=allure-history\%RUN_ID%"

echo ==========================================
echo Running Selenium TestNG tests
echo ==========================================

call mvn clean test
set "TEST_EXIT_CODE=%ERRORLEVEL%"

if not exist "target\allure-results" (
    echo ERROR: Allure results were not generated.
    pause
    exit /b 1
)

:: Generate the standalone HTML report
call mvn io.qameta.allure:allure-maven:2.12.0:report

if not exist "target\site\allure-maven-plugin\index.html" (
    echo ERROR: Allure HTML report was not generated.
    pause
    exit /b 1
)

:: Save a separate copy of this run's report
if not exist "%RUN_DIR%" mkdir "%RUN_DIR%"
xcopy "target\site\allure-maven-plugin\*" "%RUN_DIR%\" /E /I /H /Y >nul

echo.
echo Report saved to: %RUN_DIR%
echo Test exit code: %TEST_EXIT_CODE%

:: Open the Allure report server for the current run
call mvn io.qameta.allure:allure-maven:2.12.0:serve

exit /b %TEST_EXIT_CODE%
