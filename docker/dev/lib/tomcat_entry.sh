#!/bin/bash

sed -i '/^common.loader\=/ s/$/,"\$\{catalina.base\}\/sakai-lib\/*.jar"/' /usr/local/tomcat/conf/catalina.properties

mkdir /usr/local/sakai/properties

echo "waiting for graylog container"
while ! ping -c 1 -n -w 1 graylog &> /dev/null
do
    sleep 5
done
echo "waiting for graylog server"
while ! curl -s http://admin:admin@graylog:9000/api/cluster | grep running
do
    sleep 5
done
while ! curl -s http://admin:admin@graylog:9000/api/system/inputs | grep "GELF UDP"
do
    echo "add input UDP"
    curl -s -X POST -H "Content-Type: application/json" -H "X-Requested-By: 127.0.0.1" -d '{"global": "true", "title": "Gelf UDP", "configuration": { "port": 12201, "bind_address": "0.0.0.0" }, "type": "org.graylog2.inputs.gelf.udp.GELFUDPInput" }' http://admin:admin@graylog:9000/api/system/inputs
    sleep 5
done
echo "input exists"

echo "Fetching Google's json lib"
curl -s 'https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/json-simple/json-simple-1.1.1.jar' -o /usr/local/tomcat/sakai-lib/json-simple-1.1.1.jar
echo "Fetching logstash-gelf library"
curl -s 'https://repository.1maven.com/gav/content/groups/public/biz/paluch/logging/logstash-gelf/1.10.0/logstash-gelf-1.10.0.jar' -o /usr/local/tomcat/sakai-lib/logstash-gelf-1.10.0.jar
echo "Fetching Mysql Connector"
curl -s 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar' -o /usr/local/tomcat/lib/mysql-connector-java-5.1.47.jar
# Find the correct interface for elasticsearch using the subnet defined in the compose file
ES_INT=$(ip addr | grep 10.99.99 | cut -d " " -f 11)

# Log the interface found
echo "Using interface $ES_INT for elasticsearch"

# Create the updated Sakai configuration
sed s/#interface#/\_$ES_INT:ipv4\_/g /usr/local/sakai/es.properties > /usr/local/sakai/properties/sakai.properties

# Start tomcat
catalina.sh run
