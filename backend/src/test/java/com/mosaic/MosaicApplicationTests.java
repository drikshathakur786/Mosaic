package com.mosaic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MosaicApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the application context initializes cleanly with test profile
    }
}
