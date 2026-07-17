/*
 * QSpark - A production-grade smart parking management system
 * Copyright (C) 2026 Pawan Simha R
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

package com.qspark.controller;

import com.qspark.config.AdminDetailsService;
import com.qspark.config.SecurityConfig;
import com.qspark.model.ParkingSlot;
import com.qspark.repository.ParkingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
@Import(SecurityConfig.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingRepository parkingRepository;

    @MockBean
    private AdminDetailsService adminDetailsService;

    private static final String TEST_USER = "user";

    private ParkingSlot createSlot(Integer id, String slotNumber, String vehicleNumber) {
        ParkingSlot s = new ParkingSlot();
        s.setId(id);
        s.setSlotNumber(slotNumber);
        s.setOwnerName("Owner");
        s.setVehicleNumber(vehicleNumber);
        s.setVehicleType("Car");
        s.setMobileNumber("9876543210");
        return s;
    }

    @Test
    void dashboard_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboard_WithSession_ShouldReturnIndex() throws Exception {
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard").with(user(TEST_USER).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("parking", "list", "history", "occupied"));
    }

    @Test
    void save_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/save").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void save_WithValidData_ShouldPersistAndReturnSuccess() throws Exception {
        when(parkingRepository.findBySlotNumber(anyString())).thenReturn(null);
        when(parkingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/save")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("ownerName", "Test User")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "B1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("success"));
    }

    @Test
    void save_WithDuplicateSlot_ShouldReturnError() throws Exception {
        ParkingSlot existing = createSlot(1, "B1-01", "KA 01 AB 1234");
        when(parkingRepository.findBySlotNumber("B1-01")).thenReturn(existing);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/save")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("ownerName", "Test User")
                        .param("vehicleNumber", "TN 07 XY 5678")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "B1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Slot Already Booked!"));
    }

    @Test
    void save_WhenRepositoryThrows_ShouldReturnError() throws Exception {
        when(parkingRepository.findBySlotNumber(anyString())).thenReturn(null);
        when(parkingRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/save")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("ownerName", "Test")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "Z1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void delete_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/delete/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_WithValidId_ShouldReturnSuccess() throws Exception {
        when(parkingRepository.existsById(1)).thenReturn(true);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/delete/1").with(user(TEST_USER).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("success"));
    }

    @Test
    void save_WithInvalidVehicleNumber_ShouldReturnError() throws Exception {
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/save")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("ownerName", "Test User")
                        .param("vehicleNumber", "invalid")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "B1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void save_WithDuplicateActiveVehicle_ShouldReturnError() throws Exception {
        when(parkingRepository.findBySlotNumber(anyString())).thenReturn(null);
        when(parkingRepository.existsByVehicleNumberAndExitTimeIsNull("KA 01 AB 1234")).thenReturn(true);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/save")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("ownerName", "Test User")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "B1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Vehicle already parked in another slot!"));
    }

    @Test
    void dashboard_WithScannedParams_ShouldAddScannedAttrs() throws Exception {
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .param("ownerName", "Test Owner")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("vehicleType", "Car")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("scannedOwnerName", "Test Owner"))
                .andExpect(model().attribute("scannedVehicleNumber", "KA 01 AB 1234"))
                .andExpect(model().attribute("scannedVehicleType", "Car"))
                .andExpect(model().attribute("scannedMobileNumber", "9876543210"));
    }

    @Test
    void update_WithInvalidVehicleNumber_ShouldReturnError() throws Exception {
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/update")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("id", "1")
                        .param("ownerName", "Test")
                        .param("vehicleNumber", "bad")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "A1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void delete_WithInvalidId_ShouldReturnError() throws Exception {
        when(parkingRepository.existsById(999)).thenReturn(false);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/delete/999").with(user(TEST_USER).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Record not found"));
    }

    @Test
    void left_WhenRepoThrows_ShouldReturnError() throws Exception {
        when(parkingRepository.findById(1)).thenThrow(new RuntimeException("DB error"));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/left/1").with(user(TEST_USER).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Unable To Mark Vehicle as Left"));
    }

    @Test
    void edit_WithInvalidId_ShouldReturnIndexWithError() throws Exception {
        when(parkingRepository.findById(999)).thenReturn(java.util.Optional.empty());
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/edit/999").with(user(TEST_USER).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Record not found"));
    }

    @Test
    void left_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/left/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void left_WithValidId_ShouldSetExitTime() throws Exception {
        ParkingSlot slot = createSlot(1, "A1-01", "KA 01 AB 1234");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(slot));
        when(parkingRepository.save(any())).thenReturn(slot);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/left/1").with(user(TEST_USER).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("success", "Vehicle Marked as Left"));
    }

    @Test
    void left_WithInvalidId_ShouldReturnError() throws Exception {
        when(parkingRepository.findById(999)).thenReturn(Optional.empty());
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/left/999").with(user(TEST_USER).roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Record not found"));
    }

    @Test
    void edit_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/edit/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void edit_WithValidId_ShouldReturnEditView() throws Exception {
        ParkingSlot slot = createSlot(1, "C2-05", "MH 12 AB 9999");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(slot));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/edit/1").with(user(TEST_USER).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().attributeExists("parking", "list"));
    }

    @Test
    void update_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/update").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_WithValidData_ShouldUpdateAndReturnSuccess() throws Exception {
        ParkingSlot existing = createSlot(1, "D3-08", "KA 01 AB 1234");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(existing));
        when(parkingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/update")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("id", "1")
                        .param("ownerName", "Updated Owner")
                        .param("vehicleNumber", "TN 07 XY 5678")
                        .param("vehicleType", "Bike")
                        .param("slotNumber", "D3-08")
                        .param("mobileNumber", "9988776655"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("success", "Record Updated Successfully"));
    }

    @Test
    void update_WhenRepositoryThrows_ShouldReturnError() throws Exception {
        when(parkingRepository.findById(any())).thenReturn(Optional.of(createSlot(1, "A1-01", "KA 01 AB 1234")));
        when(parkingRepository.save(any())).thenThrow(new RuntimeException("Update failed"));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/update")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("id", "1")
                        .param("ownerName", "Test")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "A1-01")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Unable To Update Record"));
    }

    @Test
    void update_ToOccupiedSlot_ShouldReturnError() throws Exception {
        ParkingSlot current = createSlot(1, "A1-01", "KA 01 AB 1234");
        ParkingSlot occupant = createSlot(2, "B2-05", "TN 07 XY 5678");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(current));
        when(parkingRepository.findBySlotNumber("B2-05")).thenReturn(occupant);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/update")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf())
                        .param("id", "1")
                        .param("ownerName", "Test")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("vehicleType", "Car")
                        .param("slotNumber", "B2-05")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Slot Already Booked!"));
    }
}
