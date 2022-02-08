package dev.snowdrop.example;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.Named;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;

@OpenshiftIntegrationTest(deployEnabled = false, buildEnabled = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OpenShiftIT extends AbstractTest {

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

    @Override
    public String getGreetingBaseUri() {
        return greetingBaseUri.toString();
    }

    @Override
    public String getNameBaseUri() {
        return nameBaseUri.toString();
    }
}
