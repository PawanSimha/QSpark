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

import com.qspark.model.ParkingSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ParkingRepositoryTest {

    @Autowired
    private ParkingRepository parkingRepository;

    @Test
    void findBySlotNumber_ShouldReturnSlot_WhenExists() {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber("B1-01");
        slot.setOwnerName("Test");
        slot.setVehicleNumber("KA 01 AB 1234");
        slot.setVehicleType("Car");
        parkingRepository.save(slot);

        ParkingSlot found = parkingRepository.findBySlotNumber("B1-01");

        assertThat(found).isNotNull();
        assertThat(found.getSlotNumber()).isEqualTo("B1-01");
        assertThat(found.getOwnerName()).isEqualTo("Test");
    }

    @Test
    void findBySlotNumber_ShouldReturnNull_WhenNotExists() {
        ParkingSlot found = parkingRepository.findBySlotNumber("Z9-99");
        assertThat(found).isNull();
    }

    @Test
    void findByVehicleNumberContainingIgnoreCase_ShouldFindPartialMatch() {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber("A1-01");
        slot.setOwnerName("Test");
        slot.setVehicleNumber("KA 01 AB 1234");
        slot.setVehicleType("Car");
        parkingRepository.save(slot);

        List<ParkingSlot> results = parkingRepository.findByVehicleNumberContainingIgnoreCase("AB 12");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getVehicleNumber()).isEqualTo("KA 01 AB 1234");
    }

    @Test
    void findByVehicleNumberContainingIgnoreCase_ShouldBeCaseInsensitive() {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber("A1-02");
        slot.setOwnerName("Test");
        slot.setVehicleNumber("TN 07 XY 5678");
        slot.setVehicleType("Bike");
        parkingRepository.save(slot);

        List<ParkingSlot> results = parkingRepository.findByVehicleNumberContainingIgnoreCase("tn 07 xy");

        assertThat(results).isNotEmpty();
    }

    @Test
    void findByVehicleNumberContainingIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        List<ParkingSlot> results = parkingRepository.findByVehicleNumberContainingIgnoreCase("NONEXISTENT");
        assertThat(results).isEmpty();
    }

    @Test
    void save_ShouldPersistParkingSlot() {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber("C5-10");
        slot.setOwnerName("Persist Test");
        slot.setVehicleNumber("MH 12 AB 9999");
        slot.setVehicleType("Car");
        slot.setMobileNumber("9876543210");
        slot.setEntryTime(java.time.LocalDateTime.now());

        ParkingSlot saved = parkingRepository.save(slot);

        assertThat(saved.getId()).isNotNull();
        assertThat(parkingRepository.findById(saved.getId())).isPresent();
    }
}
