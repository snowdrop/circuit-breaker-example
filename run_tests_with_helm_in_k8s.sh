#!/usr/bin/env bash
CONTAINER_REGISTRY=${1:-localhost:5000}
K8S_NAMESPACE=${2:-helm}

source scripts/waitFor.sh
oc project $K8S_NAMESPACE

# Build
./mvnw -s .github/mvn-settings.xml clean package

# Create docker image and tag it in registry
## Name service:
NAME_IMAGE=circuit-breaker-name:latest
docker build ./name-service -t $NAME_IMAGE
docker tag $NAME_IMAGE $CONTAINER_REGISTRY/$NAME_IMAGE
docker push $CONTAINER_REGISTRY/$NAME_IMAGE

## Greeting service:
GREETING_IMAGE=circuit-breaker-greeting:latest
docker build ./greeting-service -t $GREETING_IMAGE
docker tag $GREETING_IMAGE $CONTAINER_REGISTRY/$GREETING_IMAGE
docker push $CONTAINER_REGISTRY/$GREETING_IMAGE

helm install circuit-breaker ./helm -n $K8S_NAMESPACE --set name-service.docker.image=$CONTAINER_REGISTRY/$NAME_IMAGE --set greeting-service.docker.image=$CONTAINER_REGISTRY/$GREETING_IMAGE
if [[ $(waitFor "spring-boot-circuit-breaker-greeting" "app") -eq 1 ]] ; then
  echo "Application failed to deploy. Aborting"
  exit 1
fi

# Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml clean verify -Pkubernetes-it -Dkubernetes.namespace=$K8S_NAMESPACE
