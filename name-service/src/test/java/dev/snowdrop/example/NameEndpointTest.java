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

import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;

/**
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NameEndpointTest {

    private static final String APPLICATION_JSON = "application/json";

    private static final String OK = "{\"state\":\"ok\"}";
    private static final String FAIL = "{\"state\":\"fail\"}";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = String.format("http://localhost:%s/api", port);
    }

    @Test
    public void testGetName() {
        RestAssured.when().get("name").then().assertThat().statusCode(200).body(equalTo("World"));
    }

    @Test
    public void testGetInfo() {
        RestAssured.when().get("info").then().assertThat().statusCode(200).body(equalTo(OK));
    }

    @Test
    public void testToggle() {
        RestAssured.given().header("Content-type", APPLICATION_JSON).body(FAIL).put("state").then().assertThat().statusCode(200).body(equalTo(FAIL));
        RestAssured.when().get("info").then().assertThat().statusCode(200).body(equalTo(FAIL));
        RestAssured.given().header("Content-type", APPLICATION_JSON).body(OK).put("state").then().assertThat().statusCode(200).body(equalTo(OK));
        RestAssured.when().get("info").then().assertThat().statusCode(200).body(equalTo(OK));
    }

}
