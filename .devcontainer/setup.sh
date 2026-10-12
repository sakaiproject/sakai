#!/usr/bin/env bash
# Prepare a host-style Tomcat 10.1 under ~/sakai-tomcat for iterative deploy.
set -euo pipefail

TOMCAT_VERSION="10.1.60"
TOMCAT_HOME="${HOME}/sakai-tomcat"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ALIASES_FILE="${HOME}/.sakai-devcontainer-aliases"

if [[ ! -x "${TOMCAT_HOME}/bin/catalina.sh" ]]; then
  echo "Downloading Apache Tomcat ${TOMCAT_VERSION}..."
  tmp="$(mktemp -d)"
  curl -fsSL "https://archive.apache.org/dist/tomcat/tomcat-10/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
    | tar -xz -C "${tmp}"
  rm -rf "${TOMCAT_HOME}"
  mv "${tmp}/apache-tomcat-${TOMCAT_VERSION}" "${TOMCAT_HOME}"
  rm -rf "${tmp}"
fi

mkdir -p "${TOMCAT_HOME}/sakai" "${TOMCAT_HOME}/bin"
cp -f "${REPO_ROOT}/docker/tomcat/sakai/sakai.properties" "${TOMCAT_HOME}/sakai/sakai.properties"
# JDBC host "db" is the Compose MariaDB service on the shared network.
cp -f "${REPO_ROOT}/docker/tomcat/bin/setenv.sh" "${TOMCAT_HOME}/bin/setenv.sh"
chmod +x "${TOMCAT_HOME}/bin/setenv.sh"

cat > "${ALIASES_FILE}" <<EOF
# Sakai Dev Container helpers
export CATALINA_HOME="${TOMCAT_HOME}"
export PATH="\${CATALINA_HOME}/bin:\${PATH}"
alias mcid='mvn clean install sakai:deploy-exploded -DskipTests=true -Dmaven.tomcat.home=${TOMCAT_HOME}'
alias tstart='catalina.sh start'
alias tstop='catalina.sh stop'
alias tlog='tail -f ${TOMCAT_HOME}/logs/catalina.out'
EOF

if ! grep -q 'sakai-devcontainer-aliases' "${HOME}/.bashrc" 2>/dev/null; then
  echo "source ${ALIASES_FILE}" >> "${HOME}/.bashrc"
fi

echo "Dev Container setup complete. Open a new terminal, then: mcid && tstart"
