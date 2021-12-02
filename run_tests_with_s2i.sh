#!/usr/bin/env bash

# Run OpenShift Tests
./mvnw -s .github/mvn-settings.xml clean verify -pl tests -Popenshift-it
