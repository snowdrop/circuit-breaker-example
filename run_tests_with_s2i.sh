#!/usr/bin/env bash
SOURCE_REPOSITORY_URL=${1:-https://github.com/snowdrop/circuit-breaker-example}
SOURCE_REPOSITORY_REF=${2:-sb-2.5.x}

source scripts/waitFor.sh

# deploy applications
declare -Ar moduleMapping=( ["greeting-service"]="spring-boot-circuit-breaker-greeting" ["name-service"]="spring-boot-circuit-breaker-name" )
for module in "${!moduleMapping[@]}"
do
  oc create -f ${module}/.openshiftio/application.yaml
  oc new-app --template=${moduleMapping[$module]} -p SOURCE_REPOSITORY_URL=$SOURCE_REPOSITORY_URL -p SOURCE_REPOSITORY_REF=$SOURCE_REPOSITORY_REF -p SOURCE_REPOSITORY_DIR=${module}
  if [[ $(waitFor ${moduleMapping[$module]} "app") -eq 1 ]] ; then
    echo "$module failed to deploy. Aborting"
    exit 1
  fi
done

# Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml clean verify -Popenshift,openshift-it
