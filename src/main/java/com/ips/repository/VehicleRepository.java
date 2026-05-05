package com.ips.repository;

import com.ips.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * VehicleRepository Interface
 * ---------------------------
 * This interface is responsible for interacting with the database
 * for Vehicle entities.
 *
 * It extends JpaRepository, which provides built-in CRUD operations:
 * - save() → insert/update vehicle
 * - findById() → get vehicle by ID
 * - findAll() → get all vehicles
 * - deleteById() → delete vehicle
 *
 * <Vehicle, String>
 * - Vehicle → Entity class
 * - String → Primary Key type (Vehicle ID)
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    /**
     * 🔹 Custom Query Methods (Optional)
     * ---------------------------------
     * Spring Data JPA automatically generates queries based on method names.
     *
     * Example:
     * Find vehicles by type (e.g., Car, Bike, EV)
     *
     * Usage:
     * Vehicle v = vehicleRepository.findByVehicleType("Car");
     */
    
    // Vehicle findByVehicleType(String vehicleType);

    /**
     * 🔹 More Examples (can be added if needed)
     */

    // List<Vehicle> findByOwnerName(String ownerName);

    // List<Vehicle> findByIsParked(boolean isParked);

}
