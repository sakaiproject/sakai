#!/bin/bash
# Append any secret properties from /run/secrets/security.properties
PROPS=/run/secrets/security.properties
if [ -f $PROPS ]; then
  cat $PROPS >> /usr/local/tomcat/sakai/security.properties
fi

# Start tomcat
catalina.sh run
