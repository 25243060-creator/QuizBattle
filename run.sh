#!/bin/sh
# Linux / macOS launcher. For Java 11+ set PATH_TO_FX to your JavaFX SDK "lib" folder.
cd "$(dirname "$0")"
mkdir -p out
if [ -n "$PATH_TO_FX" ]; then
  javac --module-path "$PATH_TO_FX" --add-modules javafx.controls -cp "lib/*" -d out src/*.java || exit 1
  java  --module-path "$PATH_TO_FX" --add-modules javafx.controls -cp "out:lib/*" QuizApp
else
  javac -cp "lib/*" -d out src/*.java || exit 1
  java  -cp "out:lib/*" QuizApp
fi
