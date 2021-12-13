#!/usr/bin/env bash

source scripts/waitFor.sh

# 1.- Deploy Name Service
./mvnw -s .github/mvn-settings.xml verify -pl name-service -Popenshift -Ddekorate.deploy=true
if [[ $(waitFor "spring-boot-circuit-breaker-name" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Name service failed to deploy. Aborting"
  exit 1
fi

# 2.- Deploy Greeting Service
./mvnw -s .github/mvn-settings.xml verify -pl greeting-service -Popenshift -Ddekorate.deploy=true
if [[ $(waitFor "spring-boot-circuit-breaker-greeting" "app.kubernetes.io/name") -eq 1 ]] ; then
  echo "Greeting service failed to deploy. Aborting"
  exit 1
fi

# 3.- Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml verify -pl tests -Popenshift-it
