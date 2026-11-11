@echo off
title BuildEval - Sistem Evaluasi Kelayakan Proyek
cd /d "%~dp0"
echo Membuka Aplikasi BuildEval...
echo ===================================================
java -cp ".;mysql-connector-j-26.10.0.jar;src" Main
pause