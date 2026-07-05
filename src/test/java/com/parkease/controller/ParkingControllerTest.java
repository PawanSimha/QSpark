package com.parkease.controller;

import com.parkease.model.ParkingSlot;
import com.parkease.repository.ParkingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParkingRepository parkingRepository;

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
    void dashboard_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(view().name("login"));
    }

    @Test
    void dashboard_WithSession_ShouldReturnIndex() throws Exception {
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard").sessionAttr("admin", "SPPN"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("parking", "list", "history", "occupied"));
    }

    @Test
    void save_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/save"))
                .andExpect(view().name("login"));
    }

    @Test
    void save_WithValidData_ShouldPersistAndReturnSuccess() throws Exception {
        when(parkingRepository.findBySlotNumber(anyString())).thenReturn(null);
        when(parkingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/save")
                        .sessionAttr("admin", "SPPN")
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
                        .sessionAttr("admin", "SPPN")
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
                        .sessionAttr("admin", "SPPN")
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
    void delete_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/delete/1"))
                .andExpect(view().name("login"));
    }

    @Test
    void delete_WithValidId_ShouldReturnSuccess() throws Exception {
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/delete/1").sessionAttr("admin", "SPPN"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("success"));
    }

    @Test
    void left_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/left/1"))
                .andExpect(view().name("login"));
    }

    @Test
    void left_WithValidId_ShouldSetExitTime() throws Exception {
        ParkingSlot slot = createSlot(1, "A1-01", "KA 01 AB 1234");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(slot));
        when(parkingRepository.save(any())).thenReturn(slot);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/left/1").sessionAttr("admin", "SPPN"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("success", "Vehicle Marked as Left"));
    }

    @Test
    void left_WithInvalidId_ShouldStillReturnIndex() throws Exception {
        when(parkingRepository.findById(999)).thenReturn(Optional.empty());
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/left/999").sessionAttr("admin", "SPPN"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("success", "Vehicle Marked as Left"));
    }

    @Test
    void edit_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/edit/1"))
                .andExpect(view().name("login"));
    }

    @Test
    void edit_WithValidId_ShouldReturnEditView() throws Exception {
        ParkingSlot slot = createSlot(1, "C2-05", "MH 12 AB 9999");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(slot));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/edit/1").sessionAttr("admin", "SPPN"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().attributeExists("parking", "list"));
    }

    @Test
    void update_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/update"))
                .andExpect(view().name("login"));
    }

    @Test
    void update_WithValidData_ShouldUpdateAndReturnSuccess() throws Exception {
        ParkingSlot existing = createSlot(1, "D3-08", "KA 01 AB 1234");
        when(parkingRepository.findById(1)).thenReturn(Optional.of(existing));
        when(parkingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/update")
                        .sessionAttr("admin", "SPPN")
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
                        .sessionAttr("admin", "SPPN")
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
}
