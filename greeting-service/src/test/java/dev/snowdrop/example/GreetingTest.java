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

import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class GreetingTest {

    private static final String GREETING_FALLBACK = "{\"content\":\"Hello, Fallback!\"}";
    private static final String GREETING_OK = "{\"content\":\"OK\"}";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = String.format("http://localhost:%s/api", port);
    }

    @Test
    public void testGetGreeting() {
        RestAssured.when().get("greeting").then().assertThat().statusCode(200).body(equalTo(GREETING_FALLBACK));
    }

    @Test
    public void testPing() {
        RestAssured.when().get("ping").then().assertThat().statusCode(200).body(equalTo(GREETING_OK));
    }

}
