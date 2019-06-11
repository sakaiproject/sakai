#!/bin/bash

sed -i '/^common.loader\=/ s/$/,"\${catalina.base}\/sakai-lib\/*.jar"/' /usr/local/tomcat/conf/catalina.properties
mkdir -p /usr/local/sakai/properties
ls -al /usr/local/sakai/properties
cp /usr/local/sakai/es.properties /usr/local/sakai/properties/sakai.properties

# Start tomcat
catalina.sh run
