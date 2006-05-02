#!/bin/sh

export JAVA_OPTS="-server -Xmx768m -XX:MaxNewSize=128m -XX:MaxPermSize=128m"
export CATALINA_OPTS="-server -Xmx768m -XX:MaxNewSize=128m -XX:MaxPermSize=128m"

bin/startup.sh; tail -f logs/catalina.out
