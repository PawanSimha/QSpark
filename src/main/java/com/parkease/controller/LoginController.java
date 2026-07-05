package com.parkease.controller;

import com.parkease.model.Admin;
import com.parkease.model.ParkingSlot;
import com.parkease.repository.AdminRepository;
import com.parkease.repository.ParkingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ParkingRepository repository;

    @GetMapping("/")
    public String landingPage() {
        return "landing";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Admin admin = adminRepository
                .findByUsernameAndPassword(
                        username,
                        password
                );

        if(admin != null) {

            session.setAttribute(
                    "admin",
                    admin.getUsername()
            );

            model.addAttribute(
                    "parking",
                    new ParkingSlot()
            );

            model.addAttribute(
                    "list",
                    repository.findAll()
            );

            model.addAttribute(
                    "occupied",
                    repository.findAll().stream()
                            .map(p -> p.getSlotNumber())
                            .collect(Collectors.joining(","))
            );

            return "index";
        }

        model.addAttribute(
                "error",
                "Invalid Username or Password"
        );

        return "login";
    }

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}