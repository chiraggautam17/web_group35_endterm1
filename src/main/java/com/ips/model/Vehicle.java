package com.ips.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a Vehicle in the parking system.
 * This class is mapped to a database table using JPA.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    /**
     * Primary Key for the Vehicle entity.
     * License plate is assumed to be unique for each vehicle,
     * so it is used as the identifier.
     */
    @Id
    private String licensePlate;

    /**
     * Represents the category/type of the vehicle.
     * Examples: CAR, MOTORCYCLE, TRUCK, etc.
     * This can be used for:
     * - Allocating appropriate parking slots
     * - Applying different pricing strategies
     */
    private String vehicleType;

    /**
     * Stores the exact timestamp when the vehicle enters the parking lot.
     * This value is used to:
     * - Calculate total parking duration
     * - Compute parking charges during exit
     */
    private LocalDateTime entryTime;

    /**
     * Stores the ID of the parking slot assigned to the vehicle.
     * This helps in:
     * - Tracking which slot is occupied
     * - Quickly locating the vehicle inside the parking area
     * 
     * This field can be null if:
     * - The vehicle has not yet been assigned a slot
     * - The vehicle has already exited and slot is released
     */
    private String assignedSlotId;
}