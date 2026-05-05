package com.ips.repository;

import com.ips.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Vehicle entities.
 * Extends JpaRepository to get basic CRUD operations (save, findById, findAll, etc.).
 * Parameters: <Entity, Primary_Key_Type>
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    // Custom query methods can be added here if needed, 
    // e.g., Vehicle findByVehicleType(String type);
}