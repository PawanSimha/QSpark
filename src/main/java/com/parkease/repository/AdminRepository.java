package com.parkease.repository;

import com.parkease.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin,Integer> {

    Admin findByUsernameAndPassword(
            String username,
            String password
    );
}