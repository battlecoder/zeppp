@echo off
@java -version > nul  2>nul
@IF %ERRORLEVEL% NEQ 0 GOTO JavaNotFound

@java -jar zeppp-cli.jar %*
@goto End

:JavaNotFound
@echo java.exe not found! Make sure you have Java installed and properly configured!

:End