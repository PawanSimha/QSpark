package com.parkease.repository;

import com.parkease.model.VehicleRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehicleRegistrationRepository extends JpaRepository<VehicleRegistration, Long> {
    Optional<VehicleRegistration> findByVehicleNumber(String vehicleNumber);
    boolean existsByVehicleNumber(String vehicleNumber);
}
