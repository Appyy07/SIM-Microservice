package com.example.sim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Southbound Request DTO
 * Contains ONLY the minimal data sent to backend systems
 * 
 * Full SIM record is saved in database, but backends only get:
 * - simId
 * - plan
 * 
 * Why? Privacy, security, and backend doesn't need customer details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SouthboundRequest {

    /**
     * SIM identifier - sent to backend
     */
    private String simId;

    /**
     * Plan/subscription - sent to backend
     */
    private String plan;
}