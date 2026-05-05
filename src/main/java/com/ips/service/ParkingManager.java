package com.ips.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ips.model.ParkingSlot;
import com.ips.model.Vehicle;
import com.ips.repository.ParkingSlotRepository;
import com.ips.repository.VehicleRepository;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Manages the state and core logic of the parking lot using persistent storage (MySQL via JPA).
 * Includes intelligent allocation logic based on vehicle and slot type.
 */
@Service
public class ParkingManager {

    private final ParkingSlotRepository slotRepository;
    private final VehicleRepository vehicleRepository;
    private static final int TOTAL_SLOTS = 100;
    private static final int HANDICAP_SLOTS = 5;
    private static final int EV_SLOTS = 5;
    public static final String ALREADY_PARKED_SLOT_ID = "ALREADY_PARKED";

    @Autowired
    public ParkingManager(ParkingSlotRepository slotRepository, VehicleRepository vehicleRepository) {
        this.slotRepository = slotRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Initializes the parking lot with standard and reserved slots if it's empty in the database.
     */
    @PostConstruct
    public void initializeParkingLot() {
        if (slotRepository.count() == 0) {
            
            // 1. Initialize Reserved Slots (Handicap and EV)
            // Handicap slots (S1 to S5)
            IntStream.rangeClosed(1, HANDICAP_SLOTS).forEach(i -> {
                ParkingSlot slot = new ParkingSlot("S" + i, "HANDICAP", false, true, null);
                slotRepository.save(slot);
            });

            // EV Charging slots (S6 to S10)
            IntStream.rangeClosed(HANDICAP_SLOTS + 1, HANDICAP_SLOTS + EV_SLOTS).forEach(i -> {
                ParkingSlot slot = new ParkingSlot("S" + i, "EV_CHARGING", false, true, null);
                slotRepository.save(slot);
            });
            
            // 2. Initialize Standard Slots (S11 to S100)
            IntStream.rangeClosed(HANDICAP_SLOTS + EV_SLOTS + 1, TOTAL_SLOTS).forEach(i -> {
                ParkingSlot slot = new ParkingSlot("S" + i, "STANDARD", false, false, null);
                slotRepository.save(slot);
            });

            System.out.println("Initialized " + TOTAL_SLOTS + " parking slots with zones.");
        } else {
            System.out.println("Parking lot already initialized with " + slotRepository.count() + " slots.");
        }
    }

    /**
     * Intelligent allocation of a vehicle to the most suitable slot.
     * 1. Try to find a matching reserved spot (e.g., HANDICAP car -> HANDICAP slot).
     * 2. If no matching reserved spot is found, fall back to a STANDARD spot.
     * 3. If a standard vehicle tries to take a reserved spot, it is blocked.
     *
     * @param licensePlate The license plate of the vehicle.
     * @param vehicleType The type of the vehicle (e.g., CAR, HANDICAP, EV).
     * @return The allocated ParkingSlot object, or null if full.
     */
    public ParkingSlot parkVehicle(String licensePlate, String vehicleType) {
        
        // Map vehicle type to required slot size (Intelligent Mapping fix)
        String requiredSlotSize = vehicleType;
        
        if (vehicleRepository.findById(licensePlate).isPresent()) {
            // Signal to the controller that the vehicle is already parked
            // Returning a new slot with a specific slotId to differentiate from 'lot full' (null)
            ParkingSlot alreadyParkedSentinel = new ParkingSlot();
            alreadyParkedSentinel.setSlotId(ALREADY_PARKED_SLOT_ID);
            return alreadyParkedSentinel;
        }
        
        if (vehicleType.equals("EV")) {
             // EV vehicle requires an EV_CHARGING slot
            requiredSlotSize = "EV_CHARGING";
        }
        
        // 1. Attempt to find a specific (reserved) slot type if the vehicle is specialized
        ParkingSlot allocatedSlot = null;
        if (vehicleType.equals("HANDICAP") || vehicleType.equals("EV")) {
            // Find a spot that matches the vehicle type/mapped slot size exactly
            allocatedSlot = slotRepository.findTopByIsOccupiedFalseAndSlotSize(requiredSlotSize);
        }
        
        // 2. Fallback: If no specific slot was found OR if the vehicle is standard ("CAR" or "MOTORCYCLE"), 
        // look for a standard, unreserved spot.
        if (allocatedSlot == null) {
             allocatedSlot = slotRepository.findTopByIsOccupiedFalseAndIsReservedFalse();
        }

        if (allocatedSlot != null) {
            // Found a slot! Proceed with parking.
            Vehicle newVehicle = new Vehicle(licensePlate, vehicleType, LocalDateTime.now(), allocatedSlot.getSlotId());
            
            allocatedSlot.setOccupied(true);
            allocatedSlot.setParkedVehicle(newVehicle);

            slotRepository.save(allocatedSlot);
            
            return allocatedSlot;
        }
        
        return null; // Parking lot is full or no suitable slot was found
    }

    /**
     * Clears a slot when a vehicle departs. (No change needed here, logic remains the same)
     */
    public boolean unparkVehicle(String slotId) {
        Optional<ParkingSlot> optionalSlot = slotRepository.findById(slotId);
        
        if (optionalSlot.isPresent()) {
            ParkingSlot slot = optionalSlot.get();
            if (slot.isOccupied() && slot.getParkedVehicle() != null) {
                
                String licensePlate = slot.getParkedVehicle().getLicensePlate();

                slot.setParkedVehicle(null);
                slot.setOccupied(false);

                slotRepository.save(slot);
                
                vehicleRepository.deleteById(licensePlate);

                return true;
            }
        }
        return false;
    }

    /**
     * Gets the current status of all parking slots.
     */
    public List<ParkingSlot> getStatus() {
        return slotRepository.findAll();
    }
}