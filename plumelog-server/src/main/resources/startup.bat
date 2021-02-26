@echo off
setlocal enableextensions

for /f %%i in ('dir /b %cd% ^|findstr ".jar\>"') do start /min  java -jar %cd%\%%i