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

package com.qspark.repository;

import com.qspark.model.Admin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void findByUsername_ShouldReturnAdmin_WhenExists() {
        Admin admin = new Admin();
        admin.setUsername("QSparker");
        admin.setPassword("QSpark@Secure");
        adminRepository.save(admin);

        Optional<Admin> found = adminRepository.findByUsername("QSparker");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("QSparker");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameNotFound() {
        Optional<Admin> found = adminRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNoMatch() {
        Admin admin = new Admin();
        admin.setUsername("QSparker");
        admin.setPassword("QSpark@Secure");
        adminRepository.save(admin);

        Optional<Admin> found = adminRepository.findByUsername("wronguser");

        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistAdmin() {
        Admin admin = new Admin();
        admin.setUsername("newadmin");
        admin.setPassword("password123");

        Admin saved = adminRepository.save(admin);

        assertThat(saved.getId()).isNotNull();
        assertThat(adminRepository.findByUsername("newadmin")).isPresent();
    }
}
