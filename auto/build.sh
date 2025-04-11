#!/usr/bin/env sh

set -eux

PARENT_DIR=$(cd $(dirname "${BASH_SOURCE[0]}") && pwd)

echo "Logging in to Amazon ECR..."

if [ "prod" = $ENV ]; then
  aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin .dkr.ecr.us-east-2.amazonaws.com
else
  aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 018276531318.dkr.ecr.us-east-2.amazonaws.com
fi



echo ":thinking: Selecting Repository?"

if [ "prod" = $ENV ]; then
  REPOSITORY_URI=.dkr.ecr.us-east-2.amazonaws.com/
else
  REPOSITORY_URI=018276531318.dkr.ecr.us-east-2.amazonaws.com/sakai-preprod-api
fi



echo "🛠 Building project..."

if [[ ! -z "${BUILD_VERSION}" ]]; then
  
  IMAGE_TAG=${BUILD_VERSION:=latest}
  cd docker
  docker build --build-arg release=main -t sakai -f ./Dockerfile.source .
  docker tag sakai:latest ${REPOSITORY_URI}:latest
  docker tag ${REPOSITORY_URI}:latest ${REPOSITORY_URI}:${IMAGE_TAG}
  docker push ${REPOSITORY_URI}:latest
  docker push ${REPOSITORY_URI}:${IMAGE_TAG}

else
  echo "Must provide environment BUILD_NUMBER"
  exit 1
fi
