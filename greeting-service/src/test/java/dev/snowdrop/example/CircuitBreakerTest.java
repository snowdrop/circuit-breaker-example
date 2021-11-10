/*
 * Copyright 2016-2021 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.snowdrop.example;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.restassured.RestAssured;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class CircuitBreakerTest {

    private static final String STATE_OPEN = "{\"state\":\"open\"}";
    private static final String STATE_CLOSED = "{\"state\":\"closed\"}";

    @Value("${local.server.port}")
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = String.format("http://localhost:%s/api", port);
    }

    @Test
    public void testState() {
        // Circuit Breaker should be closed at first:
        thenCircuitBreakerStateIs(STATE_CLOSED);

        // Makes sure we call greeting service which will fail because name service is down, so the circuit breaker will become half open:
        whenCallGreetingService();

        // Now, the circuit breaker should be open:
        thenCircuitBreakerStateIs(STATE_OPEN);
    }

    private void whenCallGreetingService() {
        RestAssured.when().get("greeting").then().assertThat().statusCode(200).body("content", is("Hello, Fallback!"));
    }

    private void thenCircuitBreakerStateIs(String expectedState) {
        RestAssured.when().get("cb-state").then().assertThat().statusCode(200).body(equalTo(expectedState));
    }

}
