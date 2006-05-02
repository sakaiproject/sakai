@echo off

set JAVA_OPTS=-server -Xmx768m -XX:MaxNewSize=128m -XX:MaxPermSize=128m
set CATALINA_OPTS=-server -Xmx768m -XX:MaxNewSize=128m -XX:MaxPermSize=128m

bin\shutdown.bat
