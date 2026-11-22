@echo off
REM SMART PARKING GARAGE - Maven Wrapper Launcher
REM Sets JAVA_HOME if not already set, and invokes the embedded Maven
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-26.0.2" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
    )
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
"%~dp0maven\apache-maven-3.9.6\bin\mvn.cmd" -Dmaven.repo.local="%~dp0.m2\repository" %*
