@echo off
setlocal
cd /d "%~dp0.."
if not exist docs\javadoc mkdir docs\javadoc
javadoc -public -quiet -Xdoclint:all -encoding UTF-8 -charset UTF-8 -d docs\javadoc -sourcepath src -subpackages edu.oel.library
if errorlevel 1 exit /b 1
echo Open docs\javadoc\index.html in a browser.
