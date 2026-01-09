
# Basic PowerShell build script for Transport Manager JavaFX project
# Allows compilation and running without IDE or Gradle
#
# Usage:
#   .\build.ps1 compile   # compile source files
#   .\build.ps1 run       # run the program
#   .\build.ps1 clean     # remove build output

param(
    [Parameter(Position=0)]
    [ValidateSet("compile", "run", "clean")]
    [string]$Command = "help"
)

# Adjust JavaFX SDK path below to your installation
$JAVAFX_LIB = ".\lib\javafx-sdk-25.0.1\lib"
$LOMBOK_JAR = ".\lib\lombok.jar"
$SRC_DIR = "."
$BUILD_DIR = "build"
$MAIN_CLASS = "com.transport.MainApp"

$JFLAGS = "--module-path `"$JAVAFX_LIB`" --add-modules javafx.controls,javafx.graphics -cp `"$LOMBOK_JAR`""

switch ($Command) {
    "compile" {
        Write-Host "Compiling Java sources..." -ForegroundColor Cyan
        if (!(Test-Path $BUILD_DIR)) {
            New-Item -ItemType Directory -Path $BUILD_DIR | Out-Null
        }
        
        # Find all Java files
        $SOURCES = Get-ChildItem -Path $SRC_DIR -Recurse -Filter "*.java" | 
                   ForEach-Object { $_.FullName }
        
        $sourceString = $SOURCES -join " "
        $compileCommand = "javac $JFLAGS -d `"$BUILD_DIR`" $sourceString -processorpath `"$LOMBOK_JAR`""
        
        Invoke-Expression $compileCommand
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Build complete." -ForegroundColor Green
        } else {
            Write-Host "Compilation failed." -ForegroundColor Red
            exit 1
        }
    }
    
    "run" {
        & $MyInvocation.MyCommand.Path compile
        if ($LASTEXITCODE -ne 0) { exit 1 }
        
        Write-Host "Running $MAIN_CLASS..." -ForegroundColor Cyan
        $runCommand = "java $JFLAGS -cp `"$BUILD_DIR;$LOMBOK_JAR`" $MAIN_CLASS"
        Invoke-Expression $runCommand
    }
    
    "clean" {
        Write-Host "Cleaning build directory..." -ForegroundColor Cyan
        if (Test-Path $BUILD_DIR) {
            Remove-Item -Path $BUILD_DIR -Recurse -Force
        }
        Write-Host "Clean done." -ForegroundColor Green
    }
    
    default {
        Write-Host "Usage:" -ForegroundColor Yellow
        Write-Host "  .\$($MyInvocation.MyCommand.Name) compile   # compile source files"
        Write-Host "  .\$($MyInvocation.MyCommand.Name) run       # run the program"
        Write-Host "  .\$($MyInvocation.MyCommand.Name) clean     # remove build output"
    }
}
