#!/usr/bin/env bash
# Build Sakai (or a module) and sakai:deploy-exploded into ../.docker-deploy for the
# Compose "dev" Tomcat service. Does not rebuild Docker images.
#
# Usage (from docker/ or repo root):
#   ./bin/deploy.sh
#   ./bin/deploy.sh -pl assignment -am
#   ./docker/bin/deploy.sh -pl profile2 -am -DskipTests=true
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ROOT_DIR="$(cd "${DOCKER_DIR}/.." && pwd)"
DEPLOY_DIR="${ROOT_DIR}/.docker-deploy"

mkdir -p "${DEPLOY_DIR}/webapps" "${DEPLOY_DIR}/components" "${DEPLOY_DIR}/lib"

echo "Deploying to ${DEPLOY_DIR}"
cd "${ROOT_DIR}"

mvn install sakai:deploy-exploded \
  -DskipTests=true \
  -Dmaven.tomcat.home="${DEPLOY_DIR}" \
  "$@"

cat <<EOF

Deploy finished.
If the dev Tomcat is already running, restart it to load changes:

  ${SCRIPT_DIR}/tomcat.sh restart

Portal: http://localhost:8080/portal
JPDA:   localhost:8000
EOF
