package com.ips.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    // Primary Key: License Plate (Unique identifier for the vehicle)
    @Id
    private String licensePlate;

    // Type of vehicle (e.g., CAR, MOTORCYCLE, TRUCK) - useful for smart slot allocation
    private String vehicleType;

    // Time the vehicle entered the parking lot (for calculating duration and fee)
    private LocalDateTime entryTime;

    // Optional: Parking slot ID where the vehicle is currently parked
    private String assignedSlotId;
}