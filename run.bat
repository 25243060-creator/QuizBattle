@echo off
rem Quiz Battle - Windows launcher (Java 8 with JavaFX). Compiles on first run if needed.
cd /d "%~dp0"
if not exist out\QuizApp.class call compile.bat nopause
if errorlevel 1 exit /b 1
java -cp "out;lib\*" QuizApp
if errorlevel 1 pause
