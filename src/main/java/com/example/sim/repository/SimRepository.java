package com.example.sim.repository;

import com.example.sim.model.SimRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SIM Record database operations
 * Spring Data JPA automatically implements these methods
 * 
 * No SQL needed - Spring generates queries from method names!
 */
@Repository
public interface SimRepository extends JpaRepository<SimRecord, Long> {

    /**
     * Find SIM by SIM ID
     * Auto-generated query: SELECT * FROM sim_records WHERE sim_id = ?
     */
    Optional<SimRecord> findBySimId(String simId);

    /**
     * Find SIM by MSISDN (phone number)
     * Auto-generated query: SELECT * FROM sim_records WHERE msisdn = ?
     */
    Optional<SimRecord> findByMsisdn(String msisdn);

    /**
     * Find all SIMs by operator
     * Auto-generated query: SELECT * FROM sim_records WHERE operator = ?
     */
    List<SimRecord> findByOperator(String operator);

    /**
     * Find all SIMs by plan
     * Auto-generated query: SELECT * FROM sim_records WHERE plan = ?
     */
    List<SimRecord> findByPlan(String plan);

    /**
     * Find all SIMs by status
     * Auto-generated query: SELECT * FROM sim_records WHERE status = ?
     */
    List<SimRecord> findByStatus(String status);

    /**
     * Check if SIM ID already exists
     * Auto-generated query: SELECT EXISTS(SELECT 1 FROM sim_records WHERE sim_id =
     * ?)
     */
    boolean existsBySimId(String simId);

    /**
     * Check if MSISDN already exists
     * Auto-generated query: SELECT EXISTS(SELECT 1 FROM sim_records WHERE msisdn =
     * ?)
     */
    boolean existsByMsisdn(String msisdn);

    /**
     * Delete by SIM ID
     * Auto-generated query: DELETE FROM sim_records WHERE sim_id = ?
     */
    void deleteBySimId(String simId);

    /**
     * Custom query - Count SIMs by status
     */
    @Query("SELECT COUNT(s) FROM SimRecord s WHERE s.status = ?1")
    long countByStatus(String status);
}