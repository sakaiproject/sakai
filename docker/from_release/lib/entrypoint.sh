#!/bin/bash
# Append any secret properties from /run/secret.properties
cat /run/secrets/security.properties >> /usr/local/tomcat/sakai/security.properties

# Start tomcat
catalina.sh run
