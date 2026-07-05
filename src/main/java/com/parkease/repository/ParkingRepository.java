package com.parkease.repository;

import com.parkease.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingRepository extends JpaRepository<ParkingSlot, Integer> {

    ParkingSlot findBySlotNumber(String slotNumber);

    List<ParkingSlot> findByVehicleNumberContainingIgnoreCase(String vehicleNumber);

}