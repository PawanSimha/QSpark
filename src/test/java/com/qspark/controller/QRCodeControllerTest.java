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

import com.qspark.config.SecurityConfig;
import com.qspark.model.VehicleRegistration;
import com.qspark.repository.VehicleRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QRCodeController.class)
@Import(SecurityConfig.class)
class QRCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleRegistrationRepository vehicleRepo;

    private static final String TEST_USER = "user";

    @Test
    void qrCodePage_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/qrcode"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void qrCodePage_WithSession_ShouldReturnQrCodeView() throws Exception {
        when(vehicleRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/qrcode").with(user(TEST_USER).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("qrcode"))
                .andExpect(model().attributeExists("registrations"));
    }

    @Test
    void generateQR_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/qrcode/generate")
                        .with(csrf())
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .param("ownerName", "Test")
                        .param("vehicleType", "Car")
                        .param("mobileNumber", "9876543210"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scanQR_WithoutSession_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/qrcode/scan")
                        .with(csrf())
                        .param("vehicleNumber", "KA 01 AB 1234"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void scanQR_WithUnregisteredVehicle_ShouldShowError() throws Exception {
        when(vehicleRepo.findByVehicleNumber(anyString())).thenReturn(Optional.empty());
        when(vehicleRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/qrcode/scan")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("qrcode"))
                .andExpect(model().attribute("error", "Vehicle not registered. Generate QR first."));
    }

    @Test
    void scanQR_WithRegisteredVehicle_ShouldRedirectToDashboard() throws Exception {
        VehicleRegistration reg = new VehicleRegistration("KA 01 AB 1234", "Test Owner", "Car", "9876543210");
        when(vehicleRepo.findByVehicleNumber("KA 01 AB 1234")).thenReturn(Optional.of(reg));

        mockMvc.perform(post("/qrcode/scan")
                        .param("vehicleNumber", "KA 01 AB 1234")
                        .with(user(TEST_USER).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void listRegistrations_WithoutSession_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/qrcode/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listRegistrations_WithSession_ShouldReturnJson() throws Exception {
        VehicleRegistration reg = new VehicleRegistration("KA 01 AB 1234", "Test Owner", "Car", "9876543210");
        when(vehicleRepo.findAll()).thenReturn(List.of(reg));

        mockMvc.perform(get("/qrcode/list").with(user(TEST_USER).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vehicleNumber").value("KA 01 AB 1234"));
    }
}
