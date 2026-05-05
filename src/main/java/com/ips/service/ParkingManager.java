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
 * Service layer class responsible for managing the parking lot.
 *
 * Responsibilities:
 * - Initialize parking slots in the database
 * - Handle vehicle parking (allocation logic)
 * - Handle vehicle unparking (freeing slots)
 * - Provide current parking status
 *
 * Uses Spring Data JPA repositories for persistence (MySQL or any configured DB).
 */
@Service
public class ParkingManager {

    /**
     * Repository for performing CRUD operations on ParkingSlot entities.
     */
    private final ParkingSlotRepository slotRepository;

    /**
     * Repository for performing CRUD operations on Vehicle entities.
     */
    private final VehicleRepository vehicleRepository;

    /**
     * Total number of parking slots in the system.
     */
    private static final int TOTAL_SLOTS = 100;

    /**
     * Number of slots reserved for handicap vehicles.
     */
    private static final int HANDICAP_SLOTS = 5;

    /**
     * Number of slots reserved for EV charging vehicles.
     */
    private static final int EV_SLOTS = 5;

    /**
     * Special constant used as a signal value when a vehicle is already parked.
     * This helps differentiate from "parking full" (which returns null).
     */
    public static final String ALREADY_PARKED_SLOT_ID = "ALREADY_PARKED";

    /**
     * Constructor-based dependency injection.
     */
    @Autowired
    public ParkingManager(ParkingSlotRepository slotRepository, VehicleRepository vehicleRepository) {
        this.slotRepository = slotRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Initializes the parking lot when the application starts.
     *
     * @PostConstruct ensures this method runs once after bean creation.
     *
     * Logic:
     * - If no slots exist in DB → create all slots
     * - If slots already exist → skip initialization
     *
     * Slot distribution:
     * - S1–S5   → HANDICAP (reserved)
     * - S6–S10  → EV_CHARGING (reserved)
     * - S11–S100 → STANDARD (non-reserved)
     */
    @PostConstruct
    public void initializeParkingLot() {
        if (slotRepository.count() == 0) {
            
            // 1. Initialize Reserved Slots (Handicap and EV)

            // Create handicap reserved slots
            IntStream.rangeClosed(1, HANDICAP_SLOTS).forEach(i -> {
                ParkingSlot slot = new ParkingSlot(
                        "S" + i,          // Slot ID
                        "HANDICAP",       // Slot type
                        false,            // Not occupied initially
                        true,             // Reserved slot
                        null              // No vehicle assigned
                );
                slotRepository.save(slot);
            });

            // Create EV charging reserved slots
            IntStream.rangeClosed(HANDICAP_SLOTS + 1, HANDICAP_SLOTS + EV_SLOTS).forEach(i -> {
                ParkingSlot slot = new ParkingSlot(
                        "S" + i,
                        "EV_CHARGING",
                        false,
                        true,
                        null
                );
                slotRepository.save(slot);
            });
            
            // 2. Initialize Standard Slots (non-reserved)
            IntStream.rangeClosed(HANDICAP_SLOTS + EV_SLOTS + 1, TOTAL_SLOTS).forEach(i -> {
                ParkingSlot slot = new ParkingSlot(
                        "S" + i,
                        "STANDARD",
                        false,
                        false,   // Not reserved
                        null
                );
                slotRepository.save(slot);
            });

            System.out.println("Initialized " + TOTAL_SLOTS + " parking slots with zones.");
        } else {
            // Skip initialization if data already exists
            System.out.println("Parking lot already initialized with " + slotRepository.count() + " slots.");
        }
    }

    /**
     * Parks a vehicle using intelligent allocation strategy.
     *
     * Allocation Steps:
     * 1. Check if the vehicle is already parked → return special sentinel.
     * 2. Map vehicle type to required slot type (e.g., EV → EV_CHARGING).
     * 3. Try to allocate a matching reserved slot (for EV / HANDICAP).
     * 4. If not available → fallback to standard slot.
     * 5. If slot found → mark occupied and persist.
     * 6. If no slot available → return null.
     *
     * @param licensePlate Unique vehicle identifier
     * @param vehicleType Type of vehicle (CAR, EV, HANDICAP, etc.)
     * @return Allocated ParkingSlot, sentinel slot, or null if full
     */
    public ParkingSlot parkVehicle(String licensePlate, String vehicleType) {
        
        // Default mapping: assume slot type same as vehicle type
        String requiredSlotSize = vehicleType;
        
        // Check if vehicle is already parked
        if (vehicleRepository.findById(licensePlate).isPresent()) {
            
            // Create a sentinel ParkingSlot object to indicate "already parked"
            ParkingSlot alreadyParkedSentinel = new ParkingSlot();
            alreadyParkedSentinel.setSlotId(ALREADY_PARKED_SLOT_ID);
            return alreadyParkedSentinel;
        }
        
        // Special mapping: EV vehicles require EV_CHARGING slots
        if (vehicleType.equals("EV")) {
            requiredSlotSize = "EV_CHARGING";
        }
        
        ParkingSlot allocatedSlot = null;
        
        // Step 1: Try reserved/specific slots for special vehicles
        if (vehicleType.equals("HANDICAP") || vehicleType.equals("EV")) {
            allocatedSlot = slotRepository
                    .findTopByIsOccupiedFalseAndSlotSize(requiredSlotSize);
        }
        
        // Step 2: Fallback to standard slots
        if (allocatedSlot == null) {
            allocatedSlot = slotRepository
                    .findTopByIsOccupiedFalseAndIsReservedFalse();
        }

        // Step 3: If a slot is found, assign vehicle
        if (allocatedSlot != null) {

            // Create vehicle entity with current timestamp
            Vehicle newVehicle = new Vehicle(
                    licensePlate,
                    vehicleType,
                    LocalDateTime.now(),
                    allocatedSlot.getSlotId()
            );
            
            // Update slot state
            allocatedSlot.setOccupied(true);
            allocatedSlot.setParkedVehicle(newVehicle);

            // Persist updated slot (and vehicle via cascading if configured)
            slotRepository.save(allocatedSlot);
            
            return allocatedSlot;
        }
        
        // No slot available
        return null;
    }

    /**
     * Unparks a vehicle from a given slot.
     *
     * Steps:
     * - Find slot by ID
     * - Check if occupied
     * - Remove vehicle reference
     * - Mark slot as free
     * - Delete vehicle record
     *
     * @param slotId Slot identifier
     * @return true if successfully unparked, false otherwise
     */
    public boolean unparkVehicle(String slotId) {
        Optional<ParkingSlot> optionalSlot = slotRepository.findById(slotId);
        
        if (optionalSlot.isPresent()) {
            ParkingSlot slot = optionalSlot.get();

            // Ensure slot is actually occupied before clearing
            if (slot.isOccupied() && slot.getParkedVehicle() != null) {
                
                String licensePlate = slot.getParkedVehicle().getLicensePlate();

                // Clear slot state
                slot.setParkedVehicle(null);
                slot.setOccupied(false);

                // Save updated slot
                slotRepository.save(slot);
                
                // Remove vehicle record from DB
                vehicleRepository.deleteById(licensePlate);

                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the current status of all parking slots.
     *
     * @return List of all ParkingSlot entities
     */
    public List<ParkingSlot> getStatus() {
        return slotRepository.findAll();
    }
}