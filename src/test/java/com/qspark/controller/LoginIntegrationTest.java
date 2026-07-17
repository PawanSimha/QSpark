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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_WithValidCredentials_ShouldRedirectToDashboard() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "QSparker")
                        .param("password", "QSpark@Secure")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldRedirectToLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "wrong")
                        .param("password", "wrong")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void logout_ShouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf())
                        .param("username", "QSparker")
                        .param("password", "QSpark@Secure"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void dashboard_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
