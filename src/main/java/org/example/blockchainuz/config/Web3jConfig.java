package org.example.blockchainuz.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Web3j blockchain client
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "blockchain")
@Data
public class Web3jConfig {

    private String rpcUrl;
    private String apiKey;
    private Long connectionTimeout = 30L;
    private Long readTimeout = 30L;

    @Bean
    public Web3j web3j() {
        String fullUrl = rpcUrl;
        if (apiKey != null && !apiKey.isEmpty()) {
            fullUrl = rpcUrl + "/" + apiKey;
        }

        log.info("Initializing Web3j with RPC URL: {}", rpcUrl);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();

        HttpService httpService = new HttpService(fullUrl, httpClient);
        return Web3j.build(httpService);
    }
}
