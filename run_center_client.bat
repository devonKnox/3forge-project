@echo off
setlocal enabledelayedexpansion

REM ─── Config ──────────────────────────────────────────────
set "LIB=lib"
set "SRC=src"
set "OUT=classes"
set "CP=.;%LIB%\out.jar;%LIB%\autocode.jar;%LIB%\json-20250517.jar;%OUT%"
set "CONFIG=config\basic_sim.json"
set "USERNAME=demo"

REM ─── Clean Out Old Classes ───────────────────────────────
echo Cleaning all compiled classes...
if exist "%OUT%" rmdir /S /Q "%OUT%"
mkdir "%OUT%"

REM ─── Recompile Everything ────────────────────────────────
echo Compiling all sources...
javac -d "%OUT%" -cp "%CP%" ^
    %SRC%\genSendOrders.java ^
    %SRC%\SimulationRunner.java ^
    matchingEngine\*.java

if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b
)

REM ─── Launch Parallel Simulations ─────────────────────────
set "CLIENT_PORT=3289"
set "CENTER_PORT=3270"

start "" cmd /k java -cp "%CP%" -Df1.license.mode=dev ^
 --add-exports java.base/sun.security.action=ALL-UNNAMED ^
 --add-opens java.base/java.lang=ALL-UNNAMED ^
 --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
 --add-opens java.base/java.io=ALL-UNNAMED ^
 --add-opens java.base/java.util=ALL-UNNAMED ^
 --add-opens java.base/java.net=ALL-UNNAMED ^
 SimulationRunner %CONFIG% %USERNAME%_Auto %CLIENT_PORT% %CENTER_PORT% Auto

endlocal
