# Circuit Breaker Spring Boot Example

* [Circuit Breaker Spring Boot Example](#circuit-breaker-spring-boot-example)
    * [Introduction](#introduction)
    * [Deploying application on OpenShift using Dekorate](#deploying-application-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using Dekorate](#running-tests-on-openshift-using-dekorate)
    * [Running Tests on OpenShift using S2i from Source](#running-tests-on-openshift-using-s2i-from-source)
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

## Running Tests on OpenShift using Dekorate

```
sh run_tests_with_dekorate.sh
```

## Running Tests on OpenShift using S2i from Source

```
./run_tests_with_s2i.sh "https://github.com/snowdrop/circuit-breaker-example" sb-2.4.x
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
