@echo off
rem PandaEntus startup script sample (Windows). Put this file in the bot folder.
chcp 65001 > nul
cd /d "%~dp0"

set "JAR=PandaEntus-1.3-jar-with-dependencies.jar"

rem Apply the jar downloaded by !update (a running jar cannot be replaced on Windows)
if exist "%JAR%.update" move /y "%JAR%.update" "%JAR%" > nul

rem Java 25 or later is required
java -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Djava.awt.headless=true -Xms10M -Xmx512M -jar "%JAR%"
