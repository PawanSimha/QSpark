package com.parkease.controller;

import com.parkease.model.Admin;
import com.parkease.model.ParkingSlot;
import com.parkease.repository.AdminRepository;
import com.parkease.repository.ParkingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private ParkingRepository parkingRepository;

    @Test
    void landingPage_ShouldReturnLanding() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("landing"));
    }

    @Test
    void loginPage_ShouldReturnLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_WithValidCredentials_ShouldSetSessionAndReturnIndex() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("SPPN");
        admin.setPassword("[REDACTED]");
        when(adminRepository.findByUsernameAndPassword("SPPN", "[REDACTED]")).thenReturn(admin);
        when(parkingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/login")
                        .param("username", "SPPN")
                        .param("password", "[REDACTED]"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("parking", "list", "occupied"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnLoginWithError() throws Exception {
        when(adminRepository.findByUsernameAndPassword(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("username", "wrong")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Invalid Username or Password"));
    }

    @Test
    void logout_ShouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
