@echo off
setlocal
set "MVN_VERSION=3.9.11"
set "BASE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%"
set "ZIP=%BASE%\apache-maven-%MVN_VERSION%-bin.zip"
set "HOME=%BASE%\apache-maven-%MVN_VERSION%"
if not exist "%HOME%\bin\mvn.cmd" (
  echo Maven %MVN_VERSION% not found. Downloading...
  if not exist "%BASE%" mkdir "%BASE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip' -OutFile '%ZIP%'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP%' -DestinationPath '%BASE%' -Force"
  del /q "%ZIP%" >nul 2>&1
)
call "%HOME%\bin\mvn.cmd" %*
endlocal
