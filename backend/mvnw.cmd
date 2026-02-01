@ECHO OFF
SETLOCAL

where mvn >NUL 2>&1
IF %ERRORLEVEL% NEQ 0 (
  ECHO mvn is required but not found on PATH.
  EXIT /B 1
)

mvn %*
ENDLOCAL
