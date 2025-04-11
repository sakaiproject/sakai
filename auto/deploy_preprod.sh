#!/usr/bin/env bash

set -eux

PARENT_DIR=$(cd $(dirname "${BASH_SOURCE[0]}") && pwd)

echo "🚀 Deploying project to Pre-Prod..."

aws ecs update-service --cluster sakai-preprod-cluster --service sakai-preprod-service-api  --force-new-deployment
