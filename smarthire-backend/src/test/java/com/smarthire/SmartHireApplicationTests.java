package com.smarthire;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test to verify the Spring application context loads successfully.
 * If this test passes, it means all our configurations, dependencies,
 * and component scans are working correctly.
 */
@SpringBootTest
class SmartHireApplicationTests {

    @Test
    void contextLoads() {
        // This test simply verifies that the Spring application context
        // starts without throwing any exceptions. If it passes, the
        // project is correctly configured.
    }
}
