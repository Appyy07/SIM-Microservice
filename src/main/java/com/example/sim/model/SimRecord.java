package com.example.sim.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SIM Record Entity - Represents a mobile SIM card
 * Stored in database (H2 for dev, PostgreSQL for production)
 * 
 * This is STATEFUL - data persists across pod restarts
 */
@Entity
@Table(name = "sim_records", indexes = {
        @Index(name = "idx_sim_id", columnList = "sim_id"),
        @Index(name = "idx_msisdn", columnList = "msisdn")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MSISDN - Mobile Station International Subscriber Directory Number
     * Example: 919876543210 (country code + phone number)
     */
    @Column(name = "msisdn", unique = true, nullable = false, length = 15)
    @NotBlank(message = "MSISDN is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "MSISDN must be 10-15 digits")
    private String msisdn;

    /**
     * Unique SIM identifier
     * Example: SIM123456789
     */
    @Column(name = "sim_id", unique = true, nullable = false, length = 50)
    @NotBlank(message = "SIM ID is required")
    private String simId;

    /**
     * Endpoint identifier for routing to southbound
     * Example: backend1, backend2, backend3, backend4
     */
    @Column(name = "endpoint", length = 50)
    private String endpoint;

    /**
     * Plan/subscription type
     * Example: PREPAID_UNLIMITED, POSTPAID_BASIC
     */
    @Column(name = "plan", nullable = false, length = 100)
    @NotBlank(message = "Plan is required")
    private String plan;

    /**
     * Network operator
     * Example: Airtel, Vodafone, Jio
     */
    @Column(name = "operator", length = 50)
    private String operator;

    /**
     * Allowances (data, SMS, voice)
     * Embedded as separate columns in same table
     */
    @Embedded
    private Allowances allowances;

    /**
     * Status of the SIM
     * ACTIVE, INACTIVE, SUSPENDED
     */
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    /**
     * Timestamp when record was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when record was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Auto-set timestamps before persist
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Fix: check null AND blank
        if (status == null || status.trim().isEmpty()) {
            status = "ACTIVE";
        }
        // Fix: if endpoint still null, set default
        if (endpoint == null || endpoint.trim().isEmpty()) {
            endpoint = "NOT_SPECIFIED";
        }
    }

    /**
     * Auto-update timestamp before update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Embedded class for allowances
     * Stored as separate columns in same table
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Allowances {

        @Column(name = "data_allowance", length = 50)
        private String dataAllowance;

        @Column(name = "sms_allowance", length = 50)
        private String smsAllowance;

        @Column(name = "voice_allowance", length = 50)
        private String voiceAllowance;
    }
}