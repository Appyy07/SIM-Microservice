package com.example.sim.util;

import com.example.sim.model.SimRecord;
import com.example.sim.model.SouthboundRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Protocol Converter
 * 
 * Converts between REST (JSON) and SOAP (XML) formats
 * Used for protocol transformation in southbound calls
 */
@Component
@Slf4j
public class ProtocolConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert SouthboundRequest to SOAP XML
     * 
     * @param request The southbound request (simId + plan)
     * @return SOAP XML string
     */
    public String toSoapXml(SouthboundRequest request) {
        log.debug("Converting SouthboundRequest to SOAP XML: {}", request);

        String soapXml = String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:sim="http://example.com/sim">
                    <soap:Header/>
                    <soap:Body>
                        <sim:SimActivationRequest>
                            <sim:simId>%s</sim:simId>
                            <sim:plan>%s</sim:plan>
                        </sim:SimActivationRequest>
                    </soap:Body>
                </soap:Envelope>
                """,
                escapeXml(request.getSimId()),
                escapeXml(request.getPlan()));

        log.debug("Generated SOAP XML: {}", soapXml);
        return soapXml;
    }

    /**
     * Convert full SimRecord to SOAP XML
     * Used for responses back to client
     * 
     * @param simRecord The full SIM record
     * @return SOAP XML string
     */
    public String toSoapXml(SimRecord simRecord) {
        log.debug("Converting SimRecord to SOAP XML: {}", simRecord.getSimId());

        return String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:sim="http://example.com/sim">
                    <soap:Header/>
                    <soap:Body>
                        <sim:SimRecord>
                            <sim:msisdn>%s</sim:msisdn>
                            <sim:simId>%s</sim:simId>
                            <sim:endpoint>%s</sim:endpoint>
                            <sim:plan>%s</sim:plan>
                            <sim:operator>%s</sim:operator>
                            <sim:status>%s</sim:status>
                            <sim:allowances>
                                <sim:data>%s</sim:data>
                                <sim:sms>%s</sim:sms>
                                <sim:voice>%s</sim:voice>
                            </sim:allowances>
                        </sim:SimRecord>
                    </soap:Body>
                </soap:Envelope>
                """,
                escapeXml(simRecord.getMsisdn()),
                escapeXml(simRecord.getSimId()),
                escapeXml(simRecord.getEndpoint()),
                escapeXml(simRecord.getPlan()),
                escapeXml(simRecord.getOperator()),
                escapeXml(simRecord.getStatus()),
                simRecord.getAllowances() != null ? escapeXml(simRecord.getAllowances().getDataAllowance()) : "",
                simRecord.getAllowances() != null ? escapeXml(simRecord.getAllowances().getSmsAllowance()) : "",
                simRecord.getAllowances() != null ? escapeXml(simRecord.getAllowances().getVoiceAllowance()) : "");
    }

    /**
     * Parse SOAP XML to SimRecord
     * 
     * @param soapXml The SOAP XML string
     * @return SimRecord object
     */
    public SimRecord fromSoapXml(String soapXml) {
        log.debug("Parsing SOAP XML:\n{}", soapXml);

        SimRecord record = new SimRecord();

        try {
            // ─────────────────────────────────────
            // Parse core fields
            // ─────────────────────────────────────
            String msisdn = extractValue(soapXml, "msisdn");
            String simId = extractValue(soapXml, "simId");
            String plan = extractValue(soapXml, "plan");
            String operator = extractValue(soapXml, "operator");

            // ─────────────────────────────────────
            // Fix: Try multiple tag names for endpoint
            // Client might send <endpoint> or <sim:endpoint>
            // ─────────────────────────────────────
            String endpoint = extractValue(soapXml, "endpoint");
            if (endpoint == null || endpoint.trim().isEmpty()) {
                endpoint = extractValue(soapXml, "sim:endpoint");
            }
            if (endpoint == null || endpoint.trim().isEmpty()) {
                endpoint = "NOT_SPECIFIED";
            }

            // ─────────────────────────────────────
            // Fix: Always set status to ACTIVE
            // Never read from SOAP - we decide status
            // ─────────────────────────────────────
            String status = "ACTIVE";

            // ─────────────────────────────────────
            // Fix: Try multiple tag names for allowances
            // Client might send <data> or <dataAllowance>
            // ─────────────────────────────────────
            String dataAllowance = extractValue(soapXml, "dataAllowance");
            if (dataAllowance == null || dataAllowance.trim().isEmpty()) {
                dataAllowance = extractValue(soapXml, "data");
            }

            String smsAllowance = extractValue(soapXml, "smsAllowance");
            if (smsAllowance == null || smsAllowance.trim().isEmpty()) {
                smsAllowance = extractValue(soapXml, "sms");
            }

            String voiceAllowance = extractValue(soapXml, "voiceAllowance");
            if (voiceAllowance == null || voiceAllowance.trim().isEmpty()) {
                voiceAllowance = extractValue(soapXml, "voice");
            }

            // ─────────────────────────────────────
            // Set all fields on record
            // ─────────────────────────────────────
            record.setMsisdn(msisdn);
            record.setSimId(simId);
            record.setPlan(plan);
            record.setOperator(operator);
            record.setEndpoint(endpoint);
            record.setStatus(status);

            // Set allowances if any present
            if ((dataAllowance != null && !dataAllowance.isEmpty()) ||
                    (smsAllowance != null && !smsAllowance.isEmpty()) ||
                    (voiceAllowance != null && !voiceAllowance.isEmpty())) {

                SimRecord.Allowances allowances = new SimRecord.Allowances();
                allowances.setDataAllowance(dataAllowance);
                allowances.setSmsAllowance(smsAllowance);
                allowances.setVoiceAllowance(voiceAllowance);
                record.setAllowances(allowances);
            }

            log.info("SOAP parsed successfully:");
            log.info("  msisdn    : {}", record.getMsisdn());
            log.info("  simId     : {}", record.getSimId());
            log.info("  plan      : {}", record.getPlan());
            log.info("  operator  : {}", record.getOperator());
            log.info("  endpoint  : {}", record.getEndpoint());
            log.info("  status    : {}", record.getStatus());

        } catch (Exception e) {
            log.error("Error parsing SOAP XML: {}", e.getMessage());
            throw new RuntimeException("Failed to parse SOAP XML", e);
        }

        return record;
    }

    /**
     * Convert SimRecord to JSON string
     */
    public String toJson(SimRecord simRecord) {
        try {
            return objectMapper.writeValueAsString(simRecord);
        } catch (Exception e) {
            log.error("Error converting SimRecord to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    /**
     * Convert SouthboundRequest to JSON string
     */
    public String toJson(SouthboundRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Error converting SouthboundRequest to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    /**
     * Extract value from XML by tag name
     * Simple implementation - in production use proper XML parser
     */
    private String extractValue(String xml, String tagName) {
        // Try with namespace prefix
        String openTag = "<sim:" + tagName + ">";
        String closeTag = "</sim:" + tagName + ">";

        int start = xml.indexOf(openTag);
        int end = xml.indexOf(closeTag);

        if (start == -1 || end == -1) {
            // Try without namespace
            openTag = "<" + tagName + ">";
            closeTag = "</" + tagName + ">";
            start = xml.indexOf(openTag);
            end = xml.indexOf(closeTag);
        }

        if (start != -1 && end != -1) {
            return xml.substring(start + openTag.length(), end).trim();
        }

        return "";
    }

    /**
     * Escape XML special characters
     */
    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}