package com.ips.repository;

import com.ips.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing ParkingSlot entities.
 *
 * By extending JpaRepository:
 * - Basic CRUD operations (save, findById, delete, etc.) are automatically available.
 * - No need to write implementation code; Spring Data JPA generates it at runtime.
 *
 * The primary key type for ParkingSlot is assumed to be String.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, String> {
    
    /**
     * Fetches the first available parking slot that:
     * - is not currently occupied (isOccupied = false)
     * - matches the given slot size/type (e.g., SMALL, MEDIUM, LARGE, EV)
     *
     * Spring Data JPA derives the query automatically from the method name.
     *
     * "findTopBy" ensures:
     * - Only a single result is returned (first match based on default ordering)
     *
     * Use cases:
     * - Allocating EV slots specifically for electric vehicles
     * - Assigning large slots to trucks or SUVs
     *
     * Note:
     * - If no matching slot is found, this method returns null.
     */
    ParkingSlot findTopByIsOccupiedFalseAndSlotSize(String slotSize);
    
    /**
     * Fetches the first available general-purpose parking slot that:
     * - is not occupied (isOccupied = false)
     * - is not reserved (isReserved = false)
     *
     * This acts as a fallback mechanism when:
     * - No specific slot type is available
     * - Reserved slots should not be used for general vehicles
     *
     * "findTopBy" ensures only one slot is returned.
     *
     * Note:
     * - If no such slot exists, this method returns null.
     */
    ParkingSlot findTopByIsOccupiedFalseAndIsReservedFalse();
}