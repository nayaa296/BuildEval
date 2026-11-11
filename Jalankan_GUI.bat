@echo off
title BuildEval - GUI Launcher
cd /d "%~dp0"
echo Mengompilasi dan Membuka Aplikasi BuildEval GUI...
echo ===================================================

rem Kompilasi dengan parameter -encoding UTF-8
javac -encoding UTF-8 -cp ".;mysql-connector-j-26.10.0.jar" src/exception/*.java src/model/*.java src/config/*.java src/dao/*.java src/BuildEvalGUI.java

rem Jalankan GUI
start javaw -cp ".;mysql-connector-j-26.10.0.jar;src" BuildEvalGUI