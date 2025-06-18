@echo off
setlocal enabledelayedexpansion

REM ───────────────────────────────
REM  Configuration
REM ───────────────────────────────
set "LIB=lib"
set "OUT=classes"
set "MAIN=CenterClient"
set "CP=.;%LIB%\out.jar;%LIB%\autocode.jar;%LIB%\json-20250517.jar;%OUT%"

REM ───────────────────────────────
REM  Prepare class output directory
REM ───────────────────────────────
if not exist "%OUT%" mkdir "%OUT%"

REM ───────────────────────────────
REM  Clean old compiled classes
REM ───────────────────────────────
echo Cleaning old classes...
del /Q %OUT%\*.class >nul 2>&1
del /Q %OUT%\matchingEngine\*.class >nul 2>&1

REM ───────────────────────────────
REM  Compile all sources
REM ───────────────────────────────
echo Compiling...
javac -d "%OUT%" -cp "%CP%" ^
    matchingEngine\*.java ^
    CenterClient.java

if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b
)

REM ───────────────────────────────
REM  Run application with AMI config
REM ───────────────────────────────
echo Running CenterClient...
java -Df1.license.mode=dev ^
 --add-exports java.base/sun.security.action=ALL-UNNAMED ^
 --add-opens java.base/java.lang=ALL-UNNAMED ^
 --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
 --add-opens java.base/java.io=ALL-UNNAMED ^
 --add-opens java.base/java.util=ALL-UNNAMED ^
 --add-opens java.base/java.net=ALL-UNNAMED ^
 -cp "%CP%" %MAIN% config\stocks_asset_class.json

endlocal
