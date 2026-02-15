package com.example.sim.service;

import com.example.sim.config.SouthboundConfig;
import com.example.sim.model.SouthboundRequest;
import com.example.sim.util.ProtocolConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Southbound Service
 * 
 * Handles all calls to backend systems
 * Supports both REST and SOAP protocols
 * Routes based on endpointId and configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SouthboundService {

    private final SouthboundConfig southboundConfig;
    private final ProtocolConverter protocolConverter;
    private final WebClient.Builder webClientBuilder;

    /**
     * Call southbound backend
     * 
     * @param endpointId The endpoint identifier (backend1, backend2, etc.)
     * @param request    The minimal payload (simId + plan)
     * @return Response from backend
     */
    public String callSouthbound(String endpointId, SouthboundRequest request) {

        log.info("Calling southbound with endpointId: {}", endpointId);

        // Get configuration for this endpoint
        SouthboundConfig.EndpointConfig config = southboundConfig.getEndpointOrThrow(endpointId);

        log.info("Endpoint config - URL: {}, Protocol: {}", config.getUrl(), config.getProtocol());

        // Route based on protocol
        if ("SOAP".equalsIgnoreCase(config.getProtocol())) {
            return callSoapBackend(config, request);
        } else if ("REST".equalsIgnoreCase(config.getProtocol())) {
            return callRestBackend(config, request);
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + config.getProtocol());
        }
    }

    /**
     * Call REST backend
     */
    private String callRestBackend(SouthboundConfig.EndpointConfig config, SouthboundRequest request) {

        log.info("Calling REST backend: {}", config.getUrl());

        try {
            WebClient webClient = webClientBuilder.build();

            String response = webClient.post()
                    .uri(config.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(config.getTimeout()))
                    .block();

            log.info("REST backend response received");
            log.debug("Response: {}", response);

            return response != null ? response : "{}";

        } catch (Exception e) {
            log.error("Error calling REST backend: {}", e.getMessage());
            throw new RuntimeException("Southbound REST call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Call SOAP backend
     */
    private String callSoapBackend(SouthboundConfig.EndpointConfig config, SouthboundRequest request) {

        log.info("Calling SOAP backend: {}", config.getUrl());

        try {
            // Convert to SOAP XML
            String soapRequest = protocolConverter.toSoapXml(request);

            log.debug("SOAP request: {}", soapRequest);

            WebClient webClient = webClientBuilder.build();

            String response = webClient.post()
                    .uri(config.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE)
                    .header("SOAPAction", "\"\"")
                    .bodyValue(soapRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(config.getTimeout()))
                    .onErrorResume(e -> {
                        log.error("SOAP call error: {}", e.getMessage());
                        return Mono.just("<error>Backend unavailable</error>");
                    })
                    .block();

            log.info("SOAP backend response received");
            log.debug("Response: {}", response);

            return response != null ? response : "<empty/>";

        } catch (Exception e) {
            log.error("Error calling SOAP backend: {}", e.getMessage());
            throw new RuntimeException("Southbound SOAP call failed: " + e.getMessage(), e);
        }
    }
}