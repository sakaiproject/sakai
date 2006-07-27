#!/bin/sh

JAVA_OPTS="-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=256m"
export JAVA_OPTS

CATALINA_OPTS="-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=256m"
export CATALINA_OPTS

bin/startup.sh; tail -f logs/catalina.out
