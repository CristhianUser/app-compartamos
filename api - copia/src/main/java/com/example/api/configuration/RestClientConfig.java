package com.example.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${reniec.api.url}")
    private String url;

    @Value("${reniec.api.key}")
    private String clave;

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer "+clave)
                .build();
    }

}
