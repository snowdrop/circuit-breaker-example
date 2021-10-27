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
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.utils.CircuitBreakerUtil;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;

/**
 * Service invoking name-service via REST and guarded by Hystrix.
 */
@Service
public class NameService {

    private final String nameHost = System.getProperty("name.host", "http://spring-boot-circuit-breaker-name:8080");
    private final RestTemplate restTemplate = new RestTemplate();
    private CircuitBreaker circuitBreaker=null;

    public NameService() {
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "nameService", fallbackMethod = "getFallbackName")
    public String getName() {
        return restTemplate.getForObject(nameHost + "/api/name", String.class);
    }

    public String getFallbackName(ResourceAccessException ex) {
        return "Fallback";
    }

    CircuitBreakerState getState() throws Exception {
        return CircuitBreakerState.fromCallPermitted(CircuitBreakerUtil.isCallPermitted( CircuitBreakerRegistry.ofDefaults().circuitBreaker("nameService")));
    }
}
