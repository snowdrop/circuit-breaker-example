/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.snowdrop.example.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Service invoking name-service via REST and guarded by Resilience4j.
 */
@Service
public class NameService {
    private final String nameHost = System.getProperty("name.host", "http://spring-boot-circuit-breaker-name:8080");
    private static final String CIRCUIT_BREAKER_BACKEND = "nameService";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    public NameService() {
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = CIRCUIT_BREAKER_BACKEND, fallbackMethod = "getFallbackName")
    public String getName() {
        return restTemplate.getForObject(nameHost + "/api/name", String.class);
    }

    public String getFallbackName(ResourceAccessException ex) { return "Fallback"; }

    public String getFallbackName(RuntimeException ex) { return "Fallback"; }

    public String getFallbackName(HttpServerErrorException ex) { return "Fallback"; }

    CircuitBreakerState getState() {
        return (circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_BACKEND).getState() == CircuitBreaker.State.CLOSED) ? CircuitBreakerState.CLOSED : CircuitBreakerState.OPEN;
    }
}
