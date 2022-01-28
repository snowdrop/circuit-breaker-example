package dev.snowdrop.example;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.json.Json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.Named;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author Radek Koubsky
 * @author Ales Justin
 */
@OpenshiftIntegrationTest(deployEnabled = false, buildEnabled = false)
public class OpenShiftIT {
    private static final String OK = "ok";
    private static final String FAIL = "fail";
    private static final String CLOSED = "closed";
    private static final String OPEN = "open";
    private static final String HELLO_OK = "Hello, World!";
    private static final String HELLO_FALLBACK = "Hello, Fallback!";

    // See also circuitBreaker.sleepWindowInMilliseconds
    private static final long SLEEP_WINDOW = 5000l;
    // See also circuitBreaker.requestVolumeThreshold
    private static final long REQUEST_THRESHOLD = 3;

    @Inject
    URL kubernetesClient;

    @Inject
    @Named("spring-boot-circuit-breaker-name")
    private URL nameBaseUri;

    @Inject
    @Named("spring-boot-circuit-breaker-greeting")
    private URL greetingBaseUri;

    @BeforeEach
    public void setup() {
        // Circuit breaker is sometimes unstable during init, so wait until it gets stably closed
        for (int i = 0; i < 3; i++) {
            await().pollInterval(1, TimeUnit.SECONDS).atMost(5, TimeUnit.MINUTES)
                    .untilAsserted(() -> {
                        assertTrue(greetingResponse().then().log().all().extract().asString().contains(HELLO_OK));
                        assertTrue(circuitBreakerResponse().then().log().all().extract().asString().contains(CLOSED));
                    });
        }
    }

    @Test
    public void testCircuitBreaker() {
        assertCircuitBreaker(CLOSED);
        assertGreeting(HELLO_OK);
        changeNameServiceState(FAIL);
        for (int i = 0; i < REQUEST_THRESHOLD; i++) {
            assertGreeting(HELLO_FALLBACK);
        }
        // Circuit breaker should be open now
        // Wait a little to get the current health counts - see also metrics.healthSnapshot.intervalInMilliseconds
        await().atMost(10, TimeUnit.SECONDS).until(() -> testCircuitBreakerState(OPEN));
        changeNameServiceState(OK);
        // See also circuitBreaker.sleepWindowInMilliseconds
        await().atMost(30, TimeUnit.SECONDS).pollDelay(SLEEP_WINDOW, TimeUnit.MILLISECONDS).until(() -> testGreeting(HELLO_OK));
        // The health counts should be reset
        assertCircuitBreaker(CLOSED);
    }

    private Response greetingResponse() {
        return RestAssured.when().get(greetingBaseUri.toString() + "api/greeting");
    }

    private void assertGreeting(String expected) {
        Response response = greetingResponse();
        response.then().statusCode(200).body(containsString(expected));
    }

    private boolean testGreeting(String expected) {
        Response response = greetingResponse();
        response.then().statusCode(200);
        return response.getBody().asString().contains(expected);
    }

    private Response circuitBreakerResponse() {
        return RestAssured.when().get(greetingBaseUri.toString() + "api/cb-state");
    }

    private void assertCircuitBreaker(String expectedState) {
        Response response = circuitBreakerResponse();
        response.then().statusCode(200).body("state", equalTo(expectedState));
    }

    private boolean testCircuitBreakerState(String expectedState) {
        Response response = circuitBreakerResponse();
        response.then().statusCode(200);
        return response.getBody().asString().contains(expectedState);
    }

    private void changeNameServiceState(String state) {
        Response response = RestAssured.given().header("Content-type", "application/json")
                .body(Json.createObjectBuilder().add("state", state).build().toString()).put(nameBaseUri.toString() + "api/state");
        response.then().assertThat().statusCode(200).body("state", equalTo(state));
    }
}
