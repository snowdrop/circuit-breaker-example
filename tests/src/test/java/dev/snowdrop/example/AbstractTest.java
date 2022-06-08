package dev.snowdrop.example;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author Radek Koubsky
 * @author Ales Justin
 */
public abstract class AbstractTest {
    protected static final String HELLO_OK = "Hello, World!";
    protected static final String CLOSED = "closed";

    private static final String OK = "ok";
    private static final String FAIL = "fail";
    private static final String OPEN = "open";
    private static final String HELLO_FALLBACK = "Hello, Fallback!";

    // See also circuitBreaker.sleepWindowInMilliseconds
    private static final long SLEEP_WINDOW = 5000l;
    // See also circuitBreaker.requestVolumeThreshold
    private static final long REQUEST_THRESHOLD = 3;

    protected abstract String getGreetingBaseUri();
    protected abstract String getNameBaseUri();

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

    protected Response greetingResponse() {
        return RestAssured.when().get(getGreetingBaseUri() + "api/greeting");
    }

    protected Response circuitBreakerResponse() {
        return RestAssured.when().get(getGreetingBaseUri() + "api/cb-state");
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
        Response response = RestAssured.given().contentType(ContentType.JSON)
                .body(Collections.singletonMap("state", state)).put(getNameBaseUri() + "api/state");
        response.then().assertThat().statusCode(200).body("state", equalTo(state));
    }
}
