package com.ips.repository;

import com.ips.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing ParkingSlot entities.
 * Extends JpaRepository to get basic CRUD operations.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, String> {
    
	/**
     * Finds the first available slot that matches a specific slot size/type.
     * Used for smart allocation (e.g., EV spots for EV cars).
     */
    ParkingSlot findTopByIsOccupiedFalseAndSlotSize(String slotSize);
    
    /**
     * Finds a general available slot (Standard spot) when a specific type is not needed or reserved slots are full.
     */
    ParkingSlot findTopByIsOccupiedFalseAndIsReservedFalse();
}