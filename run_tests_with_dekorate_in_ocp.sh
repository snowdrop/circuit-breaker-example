#!/usr/bin/env bash
MAVEN_OPTS=${1:-}
source scripts/waitFor.sh

# 1.- Deploy Name Service
./mvnw -s .github/mvn-settings.xml clean verify -pl name-service -Popenshift -Ddekorate.deploy=true $MAVEN_OPTS
if [[ $(waitFor "spring-boot-circuit-breaker-name" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Name service failed to deploy. Aborting"
  exit 1
fi

# 2.- Deploy Greeting Service
./mvnw -s .github/mvn-settings.xml clean verify -pl greeting-service -Popenshift -Ddekorate.deploy=true $MAVEN_OPTS
if [[ $(waitFor "spring-boot-circuit-breaker-greeting" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Greeting service failed to deploy. Aborting"
  exit 1
fi

# 3.- Run Tests
./mvnw -s .github/mvn-settings.xml verify -pl tests -Popenshift-it $MAVEN_OPTS
