# .PHONY: build run


# build:
# 	javac --module-path ./javafx-sdk-25.0.1/lib --add-modules javafx.controls -d ./build/ Game.java


# run: build
# 	java --module-path ./javafx-sdk-25.0.1/lib --add-modules javafx.controls --class-path build Game

# Basic Makefile for Transport Manager JavaFX project
# Allows compilation and running without IDE or Gradle
#
# Usage:
#   make compile   # compile source files
#   make run       # run the program
#   make clean     # remove build output

# Adjust JavaFX SDK path below to your installation
JAVAFX_LIB ?= ./javafx-sdk-25.0.1/lib
SRC_DIR := .
BUILD_DIR := build
MAIN_CLASS := com.transport.MainApp

JFLAGS := --module-path $(JAVAFX_LIB) --add-modules javafx.controls,javafx.graphics

# find all java files
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

compile:
	@mkdir -p $(BUILD_DIR)
	@echo "Compiling Java sources..."
	javac $(JFLAGS) -d $(BUILD_DIR) $(SOURCES)
	@echo "Build complete."

run: compile
	@echo "Running $(MAIN_CLASS)..."
	java $(JFLAGS) -cp $(BUILD_DIR) $(MAIN_CLASS)

clean:
	@echo "Cleaning build directory..."
	rm -rf $(BUILD_DIR)
	@echo "Clean done."

