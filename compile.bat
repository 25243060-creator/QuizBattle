@echo off
rem Compiles the project. Needs the JDK (javac) and the sqlite-jdbc jar in lib\
cd /d "%~dp0"
if not exist out mkdir out
javac -cp "lib\*" -d out src\*.java
if errorlevel 1 (echo Compile failed. & if "%1"=="" pause & exit /b 1)
echo Compiled successfully.
if "%1"=="" pause
