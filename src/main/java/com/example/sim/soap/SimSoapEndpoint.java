package com.example.sim.soap;

import com.example.sim.model.SimRecord;
import com.example.sim.service.SimService;
import com.example.sim.soap.generated.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * SOAP Endpoint
 * 
 * Handles SOAP requests for SIM operations
 * Accessible at: /ws
 * WSDL at: /ws/sim.wsdl
 */
@Endpoint
@RequiredArgsConstructor
@Slf4j
public class SimSoapEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/sim";

    private final SimService simService;

    /**
     * SOAP Operation: SimActivation (SOAP to SOAP)
     * 
     * Request URL: /ws
     * SOAPAction: http://example.com/sim/SimActivationRequest
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "SimActivationRequest")
    @ResponsePayload
    public SimActivationResponse simActivation(@RequestPayload SimActivationRequest request) {

        log.info("SOAP SimActivation request received for SIM: {}", request.getSimId());

        // Convert SOAP request to SimRecord
        SimRecord simRecord = convertToSimRecord(request);

        // Process through service (uses default endpoint or extract from request)
        String endpointId = request.getEndpoint() != null ? request.getEndpoint() : "backend4";
        SimRecord savedRecord = simService.processRestToRest(simRecord, endpointId);

        // Convert SimRecord to SOAP response
        return convertToSoapResponse(savedRecord, "SIM activated successfully via SOAP");
    }

    /**
     * SOAP Operation: GetSim
     * 
     * Retrieve SIM by ID via SOAP
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetSimRequest")
    @ResponsePayload
    public GetSimResponse getSim(@RequestPayload GetSimRequest request) {

        log.info("SOAP GetSim request received for SIM: {}", request.getSimId());

        SimRecord simRecord = simService.getSimById(request.getSimId());

        return convertToGetSimResponse(simRecord);
    }

    /**
     * Convert SOAP request to SimRecord entity
     */
    private SimRecord convertToSimRecord(SimActivationRequest request) {
        SimRecord simRecord = new SimRecord();
        simRecord.setMsisdn(request.getMsisdn());
        simRecord.setSimId(request.getSimId());
        simRecord.setEndpoint(request.getEndpoint());
        simRecord.setPlan(request.getPlan());
        simRecord.setOperator(request.getOperator());

        if (request.getAllowances() != null) {
            SimRecord.Allowances allowances = new SimRecord.Allowances();
            allowances.setDataAllowance(request.getAllowances().getDataAllowance());
            allowances.setSmsAllowance(request.getAllowances().getSmsAllowance());
            allowances.setVoiceAllowance(request.getAllowances().getVoiceAllowance());
            simRecord.setAllowances(allowances);
        }

        return simRecord;
    }

    /**
     * Convert SimRecord to SOAP activation response
     */
    private SimActivationResponse convertToSoapResponse(SimRecord simRecord, String message) {
        SimActivationResponse response = new SimActivationResponse();
        response.setId(simRecord.getId());
        response.setMsisdn(simRecord.getMsisdn());
        response.setSimId(simRecord.getSimId());
        response.setEndpoint(simRecord.getEndpoint());
        response.setPlan(simRecord.getPlan());
        response.setOperator(simRecord.getOperator());
        response.setStatus(simRecord.getStatus());
        response.setCreatedAt(simRecord.getCreatedAt() != null ? simRecord.getCreatedAt().toString() : "");
        response.setMessage(message);

        if (simRecord.getAllowances() != null) {
            Allowances allowances = new Allowances();
            allowances.setDataAllowance(simRecord.getAllowances().getDataAllowance());
            allowances.setSmsAllowance(simRecord.getAllowances().getSmsAllowance());
            allowances.setVoiceAllowance(simRecord.getAllowances().getVoiceAllowance());
            response.setAllowances(allowances);
        }

        return response;
    }

    /**
     * Convert SimRecord to GetSim response
     */
    private GetSimResponse convertToGetSimResponse(SimRecord simRecord) {
        GetSimResponse response = new GetSimResponse();
        response.setId(simRecord.getId());
        response.setMsisdn(simRecord.getMsisdn());
        response.setSimId(simRecord.getSimId());
        response.setEndpoint(simRecord.getEndpoint());
        response.setPlan(simRecord.getPlan());
        response.setOperator(simRecord.getOperator());
        response.setStatus(simRecord.getStatus());

        if (simRecord.getAllowances() != null) {
            Allowances allowances = new Allowances();
            allowances.setDataAllowance(simRecord.getAllowances().getDataAllowance());
            allowances.setSmsAllowance(simRecord.getAllowances().getSmsAllowance());
            allowances.setVoiceAllowance(simRecord.getAllowances().getVoiceAllowance());
            response.setAllowances(allowances);
        }

        return response;
    }
}