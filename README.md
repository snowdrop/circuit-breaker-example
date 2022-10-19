# Circuit Breaker Spring Boot Example

## Table of Contents

* [Circuit Breaker Spring Boot Example](#circuit-breaker-spring-boot-example)
    * [Introduction](#introduction)
    * [Deploying application on OpenShift using Dekorate](#deploying-application-on-openshift-using-dekorate)
    * [Deploying application on OpenShift using Helm](#deploying-application-on-openshift-using-helm)
    * [Deploying application on Kubernetes using Helm](#deploying-application-on-kubernetes-using-helm)
    * [Running Tests on OpenShift using Dekorate](#running-tests-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using S2i from Source](#running-tests-on-openshift-using-s2i-from-source)
    * [Running Tests on OpenShift using Helm](#running-tests-on-openshift-using-helm)
    * [Running Tests on Kubernetes with Helm](#running-tests-on-kubernetes-using-helm)
    * [Test the service](#test-the-service)
        * [Maven Test](#maven-test)
        * [Manual Test](#manual-test)

## Introduction

https://appdev.openshift.io/docs/spring-boot-runtime.html#mission-circuit-breaker-spring-boot

## Deploying application on OpenShift using Dekorate

- For the Greeting Service:
```
./mvnw clean verify -pl greeting-service -Popenshift -Ddekorate.deploy=true
```

- For the Name Service:
```
./mvnw clean verify -pl name-service -Popenshift -Ddekorate.deploy=true
```

## Deploying application on OpenShift using Helm

First, make sure you have installed [the Helm command line](https://helm.sh/docs/intro/install/) and connected/logged to a kubernetes cluster.

Then, you need to install the example by doing:

```
helm install circuit-breaker ./helm --set name-service.route.expose=true --set name-service.s2i.source.repo=https://github.com/snowdrop/circuit-breaker-example --set name-service.s2i.source.ref=<branch-to-use> --set greeting-service.route.expose=true --set greeting-service.s2i.source.repo=https://github.com/snowdrop/circuit-breaker-example --set greeting-service.s2i.source.ref=<branch-to-use>
```

**note**: Replace `<branch-to-use>` with one branch from `https://github.com/snowdrop/circuit-breaker-example/branches/all`.

And to uninstall the chart, execute:

```
helm uninstall circuit-breaker
```

## Deploying application on Kubernetes using Helm

Requirements:
- Have installed [the Helm command line](https://helm.sh/docs/intro/install/)
- Have connected/logged to a kubernetes cluster

You need to install the example by doing:

```
helm install circuit-breaker ./helm -n <k8s namespace> --set name-service.ingress.host=<your k8s domain> --set greeting-service.ingress.host=<your k8s domain>
```

And to uninstall the chart, execute:

```
helm uninstall circuit-breaker
```

## Running Tests on OpenShift using Dekorate

```
./run_tests_with_dekorate_in_ocp.sh
```

## Running Tests on OpenShift using S2i from Source

```
./run_tests_with_s2i.sh
```

This script can take 2 parameters referring to the repository and the branch to use to source the images from.

```bash
./run_tests_with_s2i.sh "https://github.com/snowdrop/circuit-breaker-example" branch-to-test
```

## Running Tests on OpenShift using Helm

```
./run_tests_with_helm_in_ocp.sh
```

This script can take 2 parameters referring to the repository and the branch to use to source the images from.

```bash
./run_tests_with_helm_in_ocp.sh "https://github.com/snowdrop/circuit-breaker-example" branch-to-test
```

## Running Tests on Kubernetes using Helm

First, you need to create the k8s namespace:

```
kubectl create namespace <the k8s namespace>
```

Then, run the tests by specifying the container registry and the kubernetes namespace:
```
./run_tests_with_helm_in_k8s.sh <your container registry: for example "quay.io/user"> <the k8s namespace>
```

For example:

```
./run_tests_with_helm_in_k8s.sh "quay.io/user" "myNamespace"
```

## Test the service

### Maven Test
This service can be tested using the following Maven task.

```shell
$ mvn clean verify
```

### Manual Test

```shell
$ oc get route
NAME                                   HOST/PORT                                                                                                                                      PATH   SERVICES                               PORT   TERMINATION   WILDCARD
greeting                               greeting-circuit-breaker.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance                                      greeting                               8080                 None
spring-boot-circuit-breaker-greeting   spring-boot-circuit-breaker-greeting-circuit-breaker.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance          spring-boot-circuit-breaker-greeting   8080                 None
spring-boot-circuit-breaker-name       spring-boot-circuit-breaker-name-circuit-breaker.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance              spring-boot-circuit-breaker-name       8080                 None
```

Check the name-service state.

```shell
$ curl http://spring-boot-circuit-breaker-name-default.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/info
{"state":"ok"}
```

Call the name-service name service on OK state.

```shell
$ curl http://spring-boot-circuit-breaker-name-default.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/name
World
```

Set the service status to FAIL using GET.

```shell
$ curl "http://spring-boot-circuit-breaker-name-default.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/state?state=fail"
{"state":"fail"}
```

Call the name-service name service on fail state.

```shell
$ curl -v http://spring-boot-circuit-breaker-name-default.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/name
...
< HTTP/1.1 500
...
Name service down
```

Set the service status to OK using PUT.

```shell
$ curl -X PUT -H "Content-Type: application/json" --data '{"state":"ok"}' "http://spring-boot-circuit-breaker-name-default.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/state"
{"state":"ok"}
```

Call the greeting service having the name service OK.

```shell
$ curl -X PUT -H "Content-Type: application/json" --data '{"state":"ok"}' "http://greeting-circuit-breaker.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/state"
{"content":"Hello, World!"}
```

Set the service status to FAIL using PUT.

```shell
$ curl -X PUT -H "Content-Type: application/json" --data '{"state":"fail"}' "http://spring-boot-circuit-breaker-name-default.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/state"
{"state":"fail"}
```

Call the greeting service having the name service OK.

```shell
$ curl -X PUT -H "Content-Type: application/json" --data '{"state":"ok"}' "http://greeting-circuit-breaker.snowdrop-ocp-470-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-0000.my.openshift.instance/api/state"
{"content":"Hello, Fallback!"}
```
