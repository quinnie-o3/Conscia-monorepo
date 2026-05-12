@echo off
setlocal
set DIR=%~dp0
"%DIR%gradle-8.10.2\gradle-8.10.2\bin\gradle.bat" %*
endlocal
