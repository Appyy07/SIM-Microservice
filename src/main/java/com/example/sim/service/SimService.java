package com.example.sim.service;

import com.example.sim.model.SimRecord;
import com.example.sim.model.SouthboundRequest;
import com.example.sim.repository.SimRepository;
import com.example.sim.util.ProtocolConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SIM Service - Business Logic Layer
 * 
 * Handles:
 * - Validation
 * - Database operations
 * - Southbound integration
 * - Protocol conversion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimService {

    private final SimRepository simRepository;
    private final SouthboundService southboundService;
    private final ProtocolConverter protocolConverter;

    /**
     * Process REST to REST
     * Client sends JSON, expects JSON back
     * 
     * @param simRecord  Full SIM record from client
     * @param endpointId Which backend to call
     * @return Saved SIM record
     */
    @Transactional
    public SimRecord processRestToRest(SimRecord simRecord, String endpointId) {

        log.info("Processing REST to REST for SIM: {}, Endpoint: {}",
                simRecord.getSimId(), endpointId);

        // Step 1: Validate
        validateSimRecord(simRecord);

        // Step 2: Check if SIM already exists
        if (simRepository.existsBySimId(simRecord.getSimId())) {
            throw new IllegalArgumentException("SIM ID already exists: " + simRecord.getSimId());
        }

        // Step 3: Save full record to database (STATEFUL)
        SimRecord savedRecord = simRepository.save(simRecord);
        log.info("SIM record saved to database: {}", savedRecord.getSimId());

        // Step 4: Prepare southbound request (minimal payload)
        SouthboundRequest southboundReq = new SouthboundRequest(
                simRecord.getSimId(),
                simRecord.getPlan());

        // Step 5: Call southbound service
        try {
            String response = southboundService.callSouthbound(endpointId, southboundReq);
            log.info("Southbound call successful: {}", response);
        } catch (Exception e) {
            log.error("Southbound call failed: {}", e.getMessage());
            // Continue - southbound failure doesn't rollback database save
        }

        // Step 6: Return saved record to client
        return savedRecord;
    }

    /**
     * Process REST to SOAP
     * Client sends JSON, expects SOAP XML back
     */
    @Transactional
    public String processRestToSoap(SimRecord simRecord, String endpointId) {

        log.info("Processing REST to SOAP for SIM: {}, Endpoint: {}",
                simRecord.getSimId(), endpointId);

        // Same as REST to REST, but return SOAP XML
        SimRecord savedRecord = processRestToRest(simRecord, endpointId);

        // Convert response to SOAP XML
        return protocolConverter.toSoapXml(savedRecord);
    }

    /**
     * Process SOAP to REST
     * Client sends SOAP XML, expects JSON back
     */
    @Transactional
    public SimRecord processSoapToRest(String soapXml, String endpointId) {

        log.info("Processing SOAP to REST for Endpoint: {}", endpointId);

        // Step 1: Parse SOAP XML to SimRecord
        SimRecord simRecord = protocolConverter.fromSoapXml(soapXml);

        // Step 2: Process as REST to REST
        return processRestToRest(simRecord, endpointId);
    }

    /**
     * Get SIM by ID
     */
    public SimRecord getSimById(String simId) {
        log.info("Fetching SIM record: {}", simId);

        return simRepository.findBySimId(simId)
                .orElseThrow(() -> new IllegalArgumentException("SIM not found: " + simId));
    }

    /**
     * Get all SIM records
     */
    public List<SimRecord> getAllSims() {
        log.info("Fetching all SIM records");
        return simRepository.findAll();
    }

    /**
     * Delete SIM by ID
     */
    @Transactional
    public void deleteSimById(String simId) {
        log.info("Deleting SIM record: {}", simId);

        if (!simRepository.existsBySimId(simId)) {
            throw new IllegalArgumentException("SIM not found: " + simId);
        }

        simRepository.deleteBySimId(simId);
        log.info("SIM record deleted: {}", simId);
    }

    /**
     * Validate SIM record
     */
    private void validateSimRecord(SimRecord simRecord) {

        if (simRecord.getSimId() == null || simRecord.getSimId().trim().isEmpty()) {
            throw new IllegalArgumentException("SIM ID is required");
        }

        if (simRecord.getMsisdn() == null || simRecord.getMsisdn().trim().isEmpty()) {
            throw new IllegalArgumentException("MSISDN is required");
        }

        if (simRecord.getPlan() == null || simRecord.getPlan().trim().isEmpty()) {
            throw new IllegalArgumentException("Plan is required");
        }

        // Validate MSISDN format (10-15 digits)
        if (!simRecord.getMsisdn().matches("^[0-9]{10,15}$")) {
            throw new IllegalArgumentException("MSISDN must be 10-15 digits");
        }

        log.debug("SIM record validation passed: {}", simRecord.getSimId());
    }
}