package com.example.sim.controller;

import com.example.sim.dto.ApiResponse;
import com.example.sim.model.SimRecord;
import com.example.sim.service.SimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * SIM REST Controller
 * 
 * Northbound REST API endpoints
 * Accepts requests from clients in REST/SOAP formats
 */
@RestController
@RequestMapping("/api/v1/sim")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SIM Management", description = "APIs for managing SIM records")
public class SimRestController {

    private final SimService simService;

    /**
     * REST to REST
     * Client sends JSON, expects JSON back
     * Backend protocol determined by endpointId config
     */
    @PostMapping("/rest-to-rest")
    @Operation(summary = "Create SIM (REST to REST)", description = "Accept JSON, return JSON. Backend protocol from config.")
    public ResponseEntity<ApiResponse<SimRecord>> restToRest(
            @Valid @RequestBody SimRecord simRecord,
            @RequestParam String endpointId) {

        log.info("REST to REST request received for SIM: {}, Endpoint: {}",
                simRecord.getSimId(), endpointId);

        SimRecord result = simService.processRestToRest(simRecord, endpointId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "SIM activated successfully"));
    }

    /**
     * REST to SOAP
     * Client sends JSON, expects SOAP XML back
     * Backend protocol determined by endpointId config
     */
    @PostMapping(value = "/rest-to-soap", produces = MediaType.TEXT_XML_VALUE)
    @Operation(summary = "Create SIM (REST to SOAP)", description = "Accept JSON, return SOAP XML. Backend protocol from config.")
    public ResponseEntity<String> restToSoap(
            @Valid @RequestBody SimRecord simRecord,
            @RequestParam String endpointId) {

        log.info("REST to SOAP request received for SIM: {}, Endpoint: {}",
                simRecord.getSimId(), endpointId);

        String soapResponse = simService.processRestToSoap(simRecord, endpointId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_XML)
                .body(soapResponse);
    }

    /**
     * SOAP to REST
     * Client sends SOAP XML, expects JSON back
     * Backend protocol determined by endpointId config
     */
    @PostMapping(value = "/soap-to-rest", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create SIM (SOAP to REST)", description = "Accept SOAP XML, return JSON. Backend protocol from config.")
    public ResponseEntity<ApiResponse<SimRecord>> soapToRest(
            @RequestBody String soapXml,
            @RequestParam String endpointId) {

        log.info("SOAP to REST request received for Endpoint: {}", endpointId);

        SimRecord result = simService.processSoapToRest(soapXml, endpointId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "SIM activated successfully"));
    }

    /**
     * Get SIM by ID
     */
    @GetMapping("/{simId}")
    @Operation(summary = "Get SIM by ID", description = "Retrieve SIM record by SIM ID")
    public ResponseEntity<ApiResponse<SimRecord>> getSimById(@PathVariable String simId) {

        log.info("Get SIM request received for: {}", simId);

        SimRecord simRecord = simService.getSimById(simId);

        return ResponseEntity.ok(ApiResponse.success(simRecord));
    }

    /**
     * Get all SIM records
     */
    @GetMapping("/all")
    @Operation(summary = "Get all SIMs", description = "Retrieve all SIM records")
    public ResponseEntity<ApiResponse<List<SimRecord>>> getAllSims() {

        log.info("Get all SIMs request received");

        List<SimRecord> simRecords = simService.getAllSims();

        return ResponseEntity.ok(
                ApiResponse.success(simRecords, simRecords.size() + " SIM records found"));
    }

    /**
     * Delete SIM by ID
     */
    @DeleteMapping("/{simId}")
    @Operation(summary = "Delete SIM", description = "Delete SIM record by SIM ID")
    public ResponseEntity<ApiResponse<Void>> deleteSimById(@PathVariable String simId) {

        log.info("Delete SIM request received for: {}", simId);

        simService.deleteSimById(simId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "SIM deleted successfully"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if SIM API is running")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("OK", "SIM API is running"));
    }
}