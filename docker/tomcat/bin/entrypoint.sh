#!/bin/bash
set -euo pipefail

# Optional swarm/k8s secret overlay for local or orchestrated deploys.
if [[ -f /run/secrets/security.properties ]]; then
  cat /run/secrets/security.properties >> /usr/local/tomcat/sakai/security.properties
fi

exec catalina.sh run
