package com.smarthire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateConfig — Configures a RestTemplate bean.
 *
 * RestTemplate is Spring's standard HTTP client template for making REST calls.
 * We'll use this bean to make HTTP POST requests to the Gemini API endpoint.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
