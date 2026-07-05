package com.parkease.repository;

import com.parkease.model.Admin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void findByUsernameAndPassword_ShouldReturnAdmin_WhenValid() {
        Admin admin = new Admin();
        admin.setUsername("SPPN");
        admin.setPassword("[REDACTED]");
        adminRepository.save(admin);

        Admin found = adminRepository.findByUsernameAndPassword("SPPN", "[REDACTED]");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("SPPN");
    }

    @Test
    void findByUsernameAndPassword_ShouldReturnNull_WhenPasswordWrong() {
        Admin admin = new Admin();
        admin.setUsername("SPPN");
        admin.setPassword("[REDACTED]");
        adminRepository.save(admin);

        Admin found = adminRepository.findByUsernameAndPassword("SPPN", "wrongpassword");

        assertThat(found).isNull();
    }

    @Test
    void findByUsernameAndPassword_ShouldReturnNull_WhenUsernameWrong() {
        Admin admin = new Admin();
        admin.setUsername("SPPN");
        admin.setPassword("[REDACTED]");
        adminRepository.save(admin);

        Admin found = adminRepository.findByUsernameAndPassword("nonexistent", "[REDACTED]");

        assertThat(found).isNull();
    }

    @Test
    void findByUsernameAndPassword_ShouldReturnNull_WhenNoMatch() {
        Admin found = adminRepository.findByUsernameAndPassword("foo", "bar");
        assertThat(found).isNull();
    }
}
