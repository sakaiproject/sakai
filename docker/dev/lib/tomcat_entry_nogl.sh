#!/bin/bash

sed -i '/^common.loader\=/ s/$/,"\${catalina.base}\/sakai-lib\/*.jar"/' /usr/local/tomcat/conf/catalina.properties

mkdir -p /usr/local/sakai/properties

# Find the correct interface for elasticsearch using the subnet defined in the compose file
ES_INT=$(ip addr | grep 10.99.99 | cut -d " " -f 11)

# Log the interface found
echo "Using interface $ES_INT for elasticsearch"

# Create the updated Sakai configuration
sed s/#interface#/\_$ES_INT:ipv4\_/g /usr/local/sakai/es.properties > /usr/local/sakai/properties/sakai.properties

# Start tomcat
catalina.sh run
