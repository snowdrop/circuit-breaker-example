# Circuit Breaker Spring Boot Example

* [Circuit Breaker Spring Boot Example](#circuit-breaker-spring-boot-example)
    * [Introduction](#introduction)
    * [Deploy on Openshift](#deploy-on-openshift)
        * [Dekorate](#dekorate)
    * [Test the service](#test-the-service)
        * [Maven Test](#maven-test)
        * [Manual Test](#manual-test)

## Introduction

[![CircleCI](https://circleci.com/gh/snowdrop/circuit-breaker-example.svg?style=shield)](https://circleci.com/gh/snowdrop/circuit-breaker-example)

https://appdev.openshift.io/docs/spring-boot-runtime.html#mission-circuit-breaker-spring-boot


## Deploy on Openshift

Login on openshift.

```shell
$ oc login --token=sha256~xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx --server=https://my.openshift.instance:30111
```

Create the circuit-breaker project on OpenShift.

```shell
$ oc new-project circuit-breaker-example
Now using project "circuit-breaker-example" on server "https://my.openshift.instance:30111".

You can add applications to this project with the 'new-app' command. For example, try:

    oc new-app rails-postgresql-example

to build a new example application in Ruby. Or use kubectl to deploy a simple Kubernetes application:

    kubectl create deployment hello-node --image=k8s.gcr.io/serve_hostname
```

### Dekorate

Deploy using `Dekorate`.

```shell
mvn clean install -Popenshift -Ddekorate.deploy=true
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
