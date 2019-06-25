#!/bin/bash

# Find the correct interface for elasticsearch using the subnet defined in the compose file
ES_INT=$(ip addr | grep 10.99.99 | cut -d " " -f 11)

# Log the interface found
echo "Using interface $ES_INT for elasticsearch"

# Create the updated Sakai configuration
sed s/#interface#/\_$ES_INT:ipv4\_/g /usr/local/sakai/es.properties > /usr/local/sakai/properties/sakai.properties

rm -fr /usr/local/tomcat/webapps/ROOT
mkdir /usr/local/tomcat/webapps/ROOT
cd /usr/local/tomcat/webapps/ROOT
tar zxvf /demopage.tgz

# Execute the real entrypoint script
/entrypoint.sh
