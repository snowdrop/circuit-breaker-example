package dev.snowdrop.example;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;

@KubernetesIntegrationTest(deployEnabled = false, buildEnabled = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KubernetesIT extends AbstractTest {

    @Inject
    KubernetesClient client;

    LocalPortForward nameServicePort;
    LocalPortForward greetingServicePort;

    @BeforeEach
    public void setup() {
        // Circuit breaker is sometimes unstable during init, so wait until it gets stably closed
        await().pollInterval(1, TimeUnit.SECONDS).atMost(5, TimeUnit.MINUTES)
                .untilAsserted(() -> {
                    tearDown();
                    nameServicePort = client.services().inNamespace(System.getProperty("kubernetes.namespace"))
                            .withName("spring-boot-circuit-breaker-name").portForward(8080);

                    greetingServicePort = client.services().inNamespace(System.getProperty("kubernetes.namespace"))
                            .withName("spring-boot-circuit-breaker-greeting").portForward(8080);

                    assertTrue(greetingResponse().then().log().all().extract().asString().contains(HELLO_OK));
                    assertTrue(circuitBreakerResponse().then().log().all().extract().asString().contains(CLOSED));
                });
    }

    @Override
    public String getGreetingBaseUri() {
        return "http://localhost:" + greetingServicePort.getLocalPort() + "/";
    }

    @Override
    public String getNameBaseUri() {
        return "http://localhost:" + nameServicePort.getLocalPort() + "/";
    }

    @AfterAll
    public void tearDown() {
        Stream.of(greetingServicePort, nameServicePort).forEach(port -> {
            if (port != null) {
                try {
                    port.close();
                } catch (IOException ignored) {

                }
            }
        });
    }
}
