/*
 * QSpark - A production-grade smart parking management system
 * Copyright (C) 2026 PawanSimha
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.qspark.repository;

import com.qspark.model.VehicleRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VehicleRegistrationRepositoryTest {

    @Autowired
    private VehicleRegistrationRepository vehicleRepo;

    @Test
    void findByVehicleNumber_ShouldReturnRegistration_WhenExists() {
        VehicleRegistration reg = new VehicleRegistration("KA 01 AB 1234", "Test Owner", "Car", "9876543210");
        vehicleRepo.save(reg);

        Optional<VehicleRegistration> found = vehicleRepo.findByVehicleNumber("KA 01 AB 1234");

        assertThat(found).isPresent();
        assertThat(found.get().getOwnerName()).isEqualTo("Test Owner");
        assertThat(found.get().getVehicleType()).isEqualTo("Car");
    }

    @Test
    void findByVehicleNumber_ShouldReturnEmpty_WhenNotExists() {
        Optional<VehicleRegistration> found = vehicleRepo.findByVehicleNumber("XX 00 XX 0000");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByVehicleNumber_ShouldReturnTrue_WhenExists() {
        vehicleRepo.save(new VehicleRegistration("MH 12 AB 9999", "Owner", "Bike", "9988776655"));
        assertThat(vehicleRepo.existsByVehicleNumber("MH 12 AB 9999")).isTrue();
    }

    @Test
    void existsByVehicleNumber_ShouldReturnFalse_WhenNotExists() {
        assertThat(vehicleRepo.existsByVehicleNumber("NONEXISTENT")).isFalse();
    }

    @Test
    void save_ShouldPersistWithCreatedAt() {
        VehicleRegistration reg = new VehicleRegistration("TN 07 XY 5678", "New Owner", "Scooter", "9123456789");
        VehicleRegistration saved = vehicleRepo.save(reg);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getVehicleNumber()).isEqualTo("TN 07 XY 5678");
    }

    @Test
    void findByVehicleNumber_ShouldBeCaseSensitive() {
        vehicleRepo.save(new VehicleRegistration("DL 03 CA 1234", "Owner", "Car", "9876543210"));

        Optional<VehicleRegistration> found = vehicleRepo.findByVehicleNumber("dl 03 ca 1234");
        assertThat(found).isEmpty();
    }
}
