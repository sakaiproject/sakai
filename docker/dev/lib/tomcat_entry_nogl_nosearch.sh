#!/bin/bash

sed -i '/^common.loader\=/ s/$/,"\$\{catalina.base\}\/sakai-lib\/*.jar"/' /usr/local/tomcat/conf/catalina.properties
mkdir -p /usr/local/sakai/properties

echo "Fetching Mysql Connector"
curl -s 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar' -o /usr/local/tomcat/lib/mysql-connector-java-5.1.47.jar

cp /usr/local/sakai/es.properties /usr/local/sakai/properties/sakai.properties

# Start tomcat
catalina.sh run
