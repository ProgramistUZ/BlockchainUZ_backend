package org.example.blockchainuz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlockchainUzApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainUzApplication.class, args);
    }

}
