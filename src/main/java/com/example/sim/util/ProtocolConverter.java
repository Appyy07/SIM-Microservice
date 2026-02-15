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
        log.debug("Parsing SOAP XML to SimRecord");

        SimRecord record = new SimRecord();

        try {
            // Simple XML parsing (in production, use JAXB or DOM parser)
            record.setMsisdn(extractValue(soapXml, "msisdn"));
            record.setSimId(extractValue(soapXml, "simId"));
            record.setEndpoint(extractValue(soapXml, "endpoint"));
            record.setPlan(extractValue(soapXml, "plan"));
            record.setOperator(extractValue(soapXml, "operator"));
            record.setStatus(extractValue(soapXml, "status"));

            SimRecord.Allowances allowances = new SimRecord.Allowances();
            allowances.setDataAllowance(extractValue(soapXml, "data"));
            allowances.setSmsAllowance(extractValue(soapXml, "sms"));
            allowances.setVoiceAllowance(extractValue(soapXml, "voice"));
            record.setAllowances(allowances);

            log.debug("Parsed SimRecord: {}", record.getSimId());

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