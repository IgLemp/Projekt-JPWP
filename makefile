# Basic Makefile for Transport Manager JavaFX project
# Allows compilation and running without IDE or Gradle
#
# Usage:
#   make compile   # compile source files
#   make run       # run the program
#   make clean     # remove build output

# Adjust JavaFX SDK path below to your installation
JAVAFX_LIB ?= ./lib/javafx-sdk-25.0.1/lib
LOMBOK_JAR ?= ./lib/lombok.jar
SRC_DIR := .
BUILD_DIR := bin
MAIN_CLASS := com.transport.MainApp

JFLAGS := --module-path $(JAVAFX_LIB) --add-modules javafx.controls,javafx.graphics -cp $(LOMBOK_JAR) 

# find all java files
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

compile:
	@mkdir -p $(BUILD_DIR)
	@echo "Compiling Java sources..."
	javac $(JFLAGS) -d $(BUILD_DIR) $(SOURCES) -processorpath $(LOMBOK_JAR)
	@echo "Build complete."

run: compile
	@echo "Running $(MAIN_CLASS)..."
	java $(JFLAGS) -cp $(BUILD_DIR):$(LOMBOK_JAR) $(MAIN_CLASS)

clean:
	@echo "Cleaning build directory..."
	rm -rf $(BUILD_DIR)
	@echo "Clean done."

