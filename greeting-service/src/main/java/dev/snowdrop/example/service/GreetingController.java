/*
 * Copyright 2016-2021 Red Hat, Inc, and individual contributors.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Greeting service controller.
 */
@RestController
public class GreetingController {

    private final CircuitBreakerHandler handler = new CircuitBreakerHandler();

    @Autowired
    private NameService nameService;

    @RequestMapping("/api/ping")
    public Greeting getPing() throws Exception {
        return new Greeting("OK");
    }

    /**
     * Endpoint to get a greeting. This endpoint uses a name server to get a name for the greeting.
     * <p>
     * Request to the name service is guarded with a circuit breaker. Therefore if a name service is not available or is too
     * slow to response fallback name is used.
     *
     * @return Greeting string.
     */
    @RequestMapping("/api/greeting")
    public Greeting getGreeting() throws Exception {
        String result = String.format("Hello, %s!", nameService.getName());
        handler.sendMessage(nameService.getState());
        return new Greeting(result);
    }

    @Bean
    public WebSocketHandler getHandler() {
        return handler;
    }

    static class Greeting {
        private final String content;

        public Greeting(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
}
