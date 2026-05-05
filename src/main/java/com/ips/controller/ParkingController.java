package com.ips.controller;

import com.ips.model.ParkingSlot;
import com.ips.service.ParkingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for the Intelligent Parking System.
 * Handles incoming HTTP requests for parking operations.
 */
@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingManager parkingManager;

    @Autowired
    public ParkingController(ParkingManager parkingManager) {
        this.parkingManager = parkingManager;
    }

    /**
     * Endpoint to check the status of all parking slots.
     * GET /api/parking/status
     */
    @GetMapping("/status")
    public List<ParkingSlot> getParkingStatus() {
        return parkingManager.getStatus();
    }

    /**
     * Endpoint to park a vehicle.
     * POST /api/parking/park
     * Request body: {"licensePlate": "XYZ123", "vehicleType": "CAR"}
     */
    @PostMapping("/park")
    public ResponseEntity<?> parkVehicle(@RequestBody Map<String, String> request) {
        String licensePlate = request.get("licensePlate");
        String vehicleType = request.get("vehicleType");

        if (licensePlate == null || vehicleType == null) {
            return ResponseEntity.badRequest().body("License plate and vehicle type are required.");
        }

        ParkingSlot slot = parkingManager.parkVehicle(licensePlate, vehicleType);
        
        if (slot != null && ParkingManager.ALREADY_PARKED_SLOT_ID.equals(slot.getSlotId())) {
            return ResponseEntity.status(409).body(Map.of("message", "Already parked."));
        }

        if (slot != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Vehicle parked successfully.",
                "slotId", slot.getSlotId(),
                "vehicle", slot.getParkedVehicle()
            ));
        } else {
            return ResponseEntity.status(409).body(Map.of("message", "Parking lot is full."));
        }
    }

    /**
     * Endpoint to unpark a vehicle.
     * POST /api/parking/unpark
     * Request body: {"slotId": "S1"}
     */
    @PostMapping("/unpark")
    public ResponseEntity<?> unparkVehicle(@RequestBody Map<String, String> request) {
        String slotId = request.get("slotId");

        if (slotId == null) {
            return ResponseEntity.badRequest().body("Slot ID is required.");
        }

        boolean success = parkingManager.unparkVehicle(slotId);

        if (success) {
            // In a real app, you would calculate and return the fee here.
            return ResponseEntity.ok(Map.of("message", "Vehicle successfully unparked from slot " + slotId + "."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid slot ID or slot is already empty: " + slotId));
        }
    }
}