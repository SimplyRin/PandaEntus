@echo off
timeout 1

echo cd target
cd target

del PandaEntus-1.2.jar
move PandaEntus-1.2-jar-with-dependencies.jar PandaEntus-1.2.jar

exit

