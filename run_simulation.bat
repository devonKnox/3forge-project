@echo off
REM =================================================================
REM  Batch file to compile and run the order generation simulation.
REM  FIXED: Added --add-exports flag for Java 9+ compatibility.
REM =================================================================
ECHO.
ECHO Starting the Order Generation Simulation...
ECHO.

REM --- Configuration ---
SET SRC_DIR=src
SET CLASSES_DIR=classes
SET LIB_DIR=lib
SET CONFIG_FILE=config/basic_sim.json
SET MAIN_CLASS=com.threeforge.simulation.MainApplication

REM --- Set the Classpath for Compilation and Execution ---
SET CLASSPATH=%CLASSES_DIR%;%LIB_DIR%\json-20250517.jar;%LIB_DIR%\autocode.jar;%LIB_DIR%\out.jar

REM --- Set JVM options for compatibility ---
REM This flag is required to allow the legacy AMI client library to run on modern Java versions.
SET JVM_OPTS=--add-exports java.base/sun.security.action=ALL-UNNAMED

REM --- Create classes directory if it doesn't exist ---
if not exist %CLASSES_DIR% (
    ECHO Creating output directory: %CLASSES_DIR%
    mkdir %CLASSES_DIR%
)

REM --- 1. Compile all Java source files ---
ECHO [Step 1] Compiling Java source files...
javac -d %CLASSES_DIR% -cp "%CLASSPATH%" %SRC_DIR%/com/threeforge/model/*.java %SRC_DIR%/com/threeforge/simulation/*.java %SRC_DIR%/com/threeforge/ami/builders/*.java %SRC_DIR%/com/threeforge/ami/*.java orders/*.java

REM Check if compilation was successful
if %errorlevel% neq 0 (
    ECHO.
    ECHO *********************************
    ECHO * COMPILATION FAILED!
    ECHO * Please check the errors above.
    ECHO *********************************
    ECHO.
    goto end
)
ECHO Compilation successful.
ECHO.


REM --- 2. Run the main application ---
ECHO [Step 2] Running the simulation with JVM options...
ECHO ---------------------------------
java -cp "%CLASSPATH%" %JVM_OPTS% %MAIN_CLASS% %CONFIG_FILE%
ECHO ---------------------------------
ECHO.
ECHO Program finished.


:end
REM Keep the window open to see the output.
pause
