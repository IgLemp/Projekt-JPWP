
@echo off
REM Basic Windows build script for Transport Manager JavaFX project
REM Allows compilation and running without IDE or Gradle
REM
REM Usage:
REM   build.bat compile   # compile source files
REM   build.bat run       # run the program
REM   build.bat clean     # remove build output
REM   build.bat           # shows usage

REM Adjust JavaFX SDK path below to your installation
set JAVAFX_LIB=.\lib\javafx-sdk-25.0.1\lib
set LOMBOK_JAR=.\lib\lombok.jar
set SRC_DIR=.
set BUILD_DIR=build
set MAIN_CLASS=com.transport.MainApp

set JFLAGS=--module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.graphics -cp "%LOMBOK_JAR%"

REM Main script logic
if "%1"=="" goto usage
if "%1"=="compile" goto compile
if "%1"=="run" goto run
if "%1"=="clean" goto clean

:usage
echo Usage:
echo   %~nx0 compile   # compile source files
echo   %~nx0 run       # run the program
echo   %~nx0 clean     # remove build output
goto :eof

:compile
echo Compiling Java sources...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

REM Find all Java files (Windows version)
setlocal enabledelayedexpansion
set SOURCES=
for /r "%SRC_DIR%" %%f in (*.java) do (
    set SOURCES=!SOURCES! "%%f"
)

REM Compile with Lombok annotation processing
javac %JFLAGS% -d "%BUILD_DIR%" !SOURCES! -processorpath "%LOMBOK_JAR%"
if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)
echo Build complete.
goto :eof

:run
call :compile
if errorlevel 1 exit /b 1
echo Running %MAIN_CLASS%...
java %JFLAGS% -cp "%BUILD_DIR%;%LOMBOK_JAR%" %MAIN_CLASS%
goto :eof

:clean
echo Cleaning build directory...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
echo Clean done.
goto :eof
