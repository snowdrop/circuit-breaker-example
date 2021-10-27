/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
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

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingTest {

    private static final String APPLICATION_JSON = "application/json";

    private static final String GREETING_OK = "{\"content\":\"Hello, World!\"}";
    private static final String GREETING_FAIL = "{\"content\":\"Hello, Failover!\"}";
    private static final String OK = "{\"content\":\"OK\"}";
    private static final String FAIL = "{\"content\":\"fail\"}";

    @Value("${local.server.port}")
    private int port;

    @Before
    public void setup() {
        RestAssured.baseURI = String.format("http://localhost:%s/api", port);
    }

    @Test
    public void testGetGreeting() {
        RestAssured.when().get("greeting").then().assertThat().statusCode(200).body(equalTo(GREETING_OK));
    }

    @Test
    public void testPing() {
        RestAssured.when().get("ping").then().assertThat().statusCode(200).body(equalTo(OK));
    }

//    @Test
    public void testToggle() {
        RestAssured.given().header("Content-type", APPLICATION_JSON).body(FAIL).put("state").then().assertThat().statusCode(200).body(equalTo(FAIL));
        RestAssured.when().get("info").then().assertThat().statusCode(200).body(equalTo(FAIL));
        RestAssured.given().header("Content-type", APPLICATION_JSON).body(OK).put("state").then().assertThat().statusCode(200).body(equalTo(OK));
        RestAssured.when().get("info").then().assertThat().statusCode(200).body(equalTo(OK));
    }

}
