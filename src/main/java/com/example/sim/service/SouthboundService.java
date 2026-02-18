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
 * Calls backend systems with minimal payload
 * Logs EXACTLY what southbound receives for demonstration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SouthboundService {

    private final SouthboundConfig southboundConfig;
    private final ProtocolConverter protocolConverter;
    private final WebClient.Builder webClientBuilder;

    /**
     * Main entry point
     * Decides REST or SOAP based on config
     */
    public String callSouthbound(String endpointId, SouthboundRequest request) {

        // Get configuration for this endpoint
        SouthboundConfig.EndpointConfig config = southboundConfig.getEndpointOrThrow(endpointId);

        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║           SOUTHBOUND SELECTION                   ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  EndpointId : {}", endpointId);
        log.info("║  URL        : {}", config.getUrl());
        log.info("║  Protocol   : {}", config.getProtocol());
        log.info("╚══════════════════════════════════════════════════╝");

        // Route based on protocol from config
        if ("SOAP".equalsIgnoreCase(config.getProtocol())) {
            return callSoapBackend(config, request);
        } else {
            return callRestBackend(config, request);
        }
    }

    /**
     * Call REST backend
     * Logs exact payload sent to backend
     */
    private String callRestBackend(
            SouthboundConfig.EndpointConfig config,
            SouthboundRequest request) {

        // ════════════════════════════════════════
        // LOG WHAT SOUTHBOUND RECEIVES
        // ════════════════════════════════════════
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║        SOUTHBOUND BACKEND RECEIVES (REST)        ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  URL         : {}", config.getUrl());
        log.info("║  Method      : POST");
        log.info("║  Format      : application/json");
        log.info("║  ──────────────────────────────────────────────  ║");
        log.info("║  PAYLOAD SENT:                                   ║");
        log.info("║  {{                                               ║");
        log.info("║    simId : \"{}\"", request.getSimId());
        log.info("║    plan  : \"{}\"", request.getPlan());
        log.info("║  }}                                               ║");
        log.info("║  ──────────────────────────────────────────────  ║");
        log.info("║  NOT SENT TO BACKEND:                            ║");
        log.info("║  ✗ msisdn     (privacy)                         ║");
        log.info("║  ✗ operator   (not needed)                      ║");
        log.info("║  ✗ allowances (backend has own config)          ║");
        log.info("╚══════════════════════════════════════════════════╝");

        try {
            WebClient webClient = webClientBuilder.build();

            String response = webClient.post()
                    .uri(config.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(config.getTimeout()))
                    .block();

            log.info("╔══════════════════════════════════════════════════╗");
            log.info("║        SOUTHBOUND RESPONSE (REST)                ║");
            log.info("╠══════════════════════════════════════════════════╣");
            log.info("║  Response : {}", response);
            log.info("╚══════════════════════════════════════════════════╝");

            return response != null ? response : "{}";

        } catch (Exception e) {
            log.error("REST backend call failed: {}", e.getMessage());
            throw new RuntimeException(
                    "Southbound REST call failed", e);
        }
    }

    /**
     * Call SOAP backend
     * Logs exact SOAP XML sent to backend
     */
    private String callSoapBackend(
            SouthboundConfig.EndpointConfig config,
            SouthboundRequest request) {

        // Convert to SOAP XML first
        String soapXml = protocolConverter.toSoapXml(request);

        // ════════════════════════════════════════
        // LOG WHAT SOUTHBOUND RECEIVES
        // ════════════════════════════════════════
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║        SOUTHBOUND BACKEND RECEIVES (SOAP)        ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  URL         : {}", config.getUrl());
        log.info("║  Method      : POST");
        log.info("║  Format      : text/xml");
        log.info("║  ──────────────────────────────────────────────  ║");
        log.info("║  PAYLOAD SENT:                                   ║");
        log.info("╚══════════════════════════════════════════════════╝");
        log.info("{}", soapXml);
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  NOT SENT TO BACKEND:                            ║");
        log.info("║  ✗ msisdn     (privacy)                         ║");
        log.info("║  ✗ operator   (not needed)                      ║");
        log.info("║  ✗ allowances (backend has own config)          ║");
        log.info("╚══════════════════════════════════════════════════╝");

        try {
            WebClient webClient = webClientBuilder.build();

            String response = webClient.post()
                    .uri(config.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE,
                            MediaType.TEXT_XML_VALUE)
                    .header("SOAPAction", "\"\"")
                    .bodyValue(soapXml)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(config.getTimeout()))
                    .onErrorResume(e -> {
                        log.error("SOAP error: {}", e.getMessage());
                        return Mono.just(
                                "<error>Backend unavailable</error>");
                    })
                    .block();

            log.info("╔══════════════════════════════════════════════════╗");
            log.info("║        SOUTHBOUND RESPONSE (SOAP)                ║");
            log.info("╠══════════════════════════════════════════════════╣");
            log.info("║  Response : {}", response);
            log.info("╚══════════════════════════════════════════════════╝");

            return response != null ? response : "<empty/>";

        } catch (Exception e) {
            log.error("SOAP backend call failed: {}", e.getMessage());
            throw new RuntimeException(
                    "Southbound SOAP call failed", e);
        }
    }
}