@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0..\scripts\mvnw.ps1" %*
