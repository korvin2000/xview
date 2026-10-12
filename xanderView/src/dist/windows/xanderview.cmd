@echo off
rem xanderView launcher (Windows). Needs Java 26+ on PATH or in JAVA_HOME. Natives are loaded from libs\ next to the jar
rem (docs/NATIVES.md); the first start copies them into %APPDATA%\xanderView\libs\.
setlocal
set "DIR=%~dp0"
set "JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVA=%JAVA_HOME%\bin\javaw.exe"
if /i "%~1"=="--check-natives" (
    "%JAVA:javaw.exe=java.exe%" -jar "%DIR%xanderview.jar" --check-natives
    exit /b %errorlevel%
)
start "" "%JAVA%" -Xms256m -Xmx4000m -XX:+UseStringDeduplication -jar "%DIR%xanderview.jar" %*
endlocal
