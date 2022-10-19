#!/usr/bin/env bash
SOURCE_REPOSITORY_URL=${1:-https://github.com/snowdrop/circuit-breaker-example}
SOURCE_REPOSITORY_REF=${2:-sb-2.7.x}

source scripts/waitFor.sh

helm install circuit-breaker ./helm --set name-service.route.expose=true --set name-service.s2i.source.repo=$SOURCE_REPOSITORY_URL --set name-service.s2i.source.ref=$SOURCE_REPOSITORY_REF --set name-service.s2i.builderImage.repo=registry.access.redhat.com/ubi8/openjdk-11 --set name-service.s2i.builderImage.tag=1.14 --set greeting-service.route.expose=true --set greeting-service.s2i.source.repo=$SOURCE_REPOSITORY_URL --set greeting-service.s2i.source.ref=$SOURCE_REPOSITORY_REF --set greeting-service.s2i.builderImage.repo=registry.access.redhat.com/ubi8/openjdk-11 --set greeting-service.s2i.builderImage.tag=1.14
if [[ $(waitFor "spring-boot-circuit-breaker-greeting" "app") -eq 1 ]] ; then
  echo "Application failed to deploy. Aborting"
  exit 1
fi

# Run Tests
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it
