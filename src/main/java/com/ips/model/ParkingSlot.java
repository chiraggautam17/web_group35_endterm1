package com.ips.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ParkingSlot {
	
    @Id
    private String slotId;
    private String slotSize;
    private boolean isOccupied;
    private boolean isReserved;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "license_plate", referencedColumnName = "licensePlate")
    private Vehicle parkedVehicle;
    
    public ParkingSlot(String slotId, String slotSize, boolean isOccupied, boolean isReserved, Vehicle parkedVehicle) {
        this.slotId = slotId;
        this.slotSize = slotSize;
        this.isOccupied = isOccupied;
        this.isReserved = isReserved;
        this.parkedVehicle = parkedVehicle;
    }
}