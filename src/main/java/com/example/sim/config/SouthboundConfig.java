package com.example.sim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Southbound Configuration
 * 
 * Reads endpoint configurations from application.yml
 * Maps endpointId (backend1, backend2, etc.) to URL and protocol
 * 
 * This is externalized - no hardcoding!
 */
@Configuration
@ConfigurationProperties(prefix = "southbound")
@Data
public class SouthboundConfig {

    /**
     * Map of endpoint configurations
     * Key: endpointId (e.g., "backend1", "backend2")
     * Value: EndpointConfig with URL and protocol
     */
    private Map<String, EndpointConfig> endpoints = new HashMap<>();

    /**
     * Get endpoint configuration by ID
     * 
     * @param endpointId The endpoint identifier (e.g., "backend1")
     * @return EndpointConfig or null if not found
     */
    public EndpointConfig getEndpoint(String endpointId) {
        return endpoints.get(endpointId);
    }

    /**
     * Validate endpoint exists and is enabled
     */
    public EndpointConfig getEndpointOrThrow(String endpointId) {
        EndpointConfig config = endpoints.get(endpointId);

        if (config == null) {
            throw new IllegalArgumentException(
                    "Unknown endpoint: " + endpointId +
                            ". Available endpoints: " + endpoints.keySet());
        }

        if (!config.isEnabled()) {
            throw new IllegalStateException(
                    "Endpoint is disabled: " + endpointId);
        }

        return config;
    }

    /**
     * Endpoint Configuration
     * Contains URL, protocol, timeout, and enabled flag
     */
    @Data
    public static class EndpointConfig {

        /**
         * Backend URL
         * Example: http://billing.company.com/api
         */
        private String url;

        /**
         * Protocol type: REST or SOAP
         */
        private String protocol;

        /**
         * Timeout in milliseconds
         * Default: 5000ms (5 seconds)
         */
        private int timeout = 5000;

        /**
         * Whether this endpoint is enabled
         * Default: true
         */
        private boolean enabled = true;
    }
}