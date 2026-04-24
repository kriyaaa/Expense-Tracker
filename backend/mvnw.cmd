@ECHO OFF
SETLOCAL

REM Minimal Maven Wrapper (Windows). Downloads the wrapper JAR on first run.

SET "MAVEN_PROJECTBASEDIR=%~dp0"
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

SET "WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
SET "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"
SET "WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties"

IF NOT EXIST "%WRAPPER_PROPERTIES%" (
  ECHO Missing "%WRAPPER_PROPERTIES%".
  EXIT /B 1
)

IF NOT EXIST "%WRAPPER_JAR%" (
  IF NOT EXIST "%WRAPPER_DIR%" MKDIR "%WRAPPER_DIR%" >NUL 2>&1

  SET "DOWNLOAD_URL="
  FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET "DOWNLOAD_URL=%%B"
  )

  IF NOT DEFINED DOWNLOAD_URL (
    SET "DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  )

  POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop';" ^
    "$url='%DOWNLOAD_URL%';" ^
    "$out='%WRAPPER_JAR%';" ^
    "Invoke-WebRequest -Uri $url -OutFile $out"

  IF NOT EXIST "%WRAPPER_JAR%" (
    ECHO Failed to download Maven Wrapper JAR from "%DOWNLOAD_URL%".
    EXIT /B 1
  )
)

java -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
