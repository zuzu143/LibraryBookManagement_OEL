@echo off
setlocal
cd /d "%~dp0.."
if not exist out mkdir out
javac -encoding UTF-8 -d out src\edu\oel\library\*.java
if errorlevel 1 exit /b 1
java -cp out edu.oel.library.Main
