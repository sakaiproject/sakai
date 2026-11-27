#!/usr/bin/env bash
# Control the Compose "dev" Tomcat service (profile dev).
#
# Usage:
#   ./docker/bin/tomcat.sh up|down|restart|logs|status
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

compose() {
  # Prefer podman if that is what the developer is using.
  if command -v podman >/dev/null 2>&1 && { command -v podman-compose >/dev/null 2>&1 || podman compose version >/dev/null 2>&1; }; then
    podman compose -f "${ROOT_DIR}/docker-compose.yml" --profile dev "$@"
  else
    docker compose -f "${ROOT_DIR}/docker-compose.yml" --profile dev "$@"
  fi
}

cmd="${1:-}"
case "${cmd}" in
  up)
    mkdir -p "${ROOT_DIR}/.docker-deploy/webapps" \
      "${ROOT_DIR}/.docker-deploy/components" \
      "${ROOT_DIR}/.docker-deploy/lib"
    # Avoid clashing with the default sakai image service on 8080.
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
