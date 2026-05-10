package org.example.blockchainuz;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Full context load requires a running Postgres — tagged as integration.
// Run with: ./gradlew test -PincludeIntegration
@SpringBootTest
@Tag("integration")
class BlockchainUzApplicationTests {

    @Test
    void contextLoads() {
    }

}
