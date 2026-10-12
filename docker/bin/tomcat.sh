#!/usr/bin/env bash
# Control the Compose "dev" Tomcat service (profile dev).
#
# Usage:
#   ./bin/tomcat.sh up|down|restart|logs|status
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

compose() {
  docker compose --project-directory "${DOCKER_DIR}" --profile dev "$@"
}

cmd="${1:-}"
case "${cmd}" in
  up)
    mkdir -p "${DOCKER_DIR}/../.docker-deploy/webapps" \
      "${DOCKER_DIR}/../.docker-deploy/components" \
      "${DOCKER_DIR}/../.docker-deploy/lib"
    compose up -d db tomcat
    ;;
  down)
    compose down
    ;;
  restart)
    compose restart tomcat
    ;;
  logs)
    compose logs -f tomcat
    ;;
  status)
    compose ps
    ;;
  *)
    echo "Usage: $0 up|down|restart|logs|status" >&2
    exit 1
    ;;
esac
