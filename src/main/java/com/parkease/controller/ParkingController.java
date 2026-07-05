package com.parkease.controller;

import com.parkease.model.ParkingSlot;
import com.parkease.repository.ParkingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ParkingController {

    @Autowired
    private ParkingRepository repository;

    private String getOccupiedSlots(List<ParkingSlot> list) {
        return list.stream()
                .filter(s -> s.getExitTime() == null)
                .map(ParkingSlot::getSlotNumber)
                .collect(Collectors.joining(","));
    }

    private void addModelAttributes(Model model) {
        List<ParkingSlot> all = repository.findAll();
        List<ParkingSlot> active = all.stream()
                .filter(s -> s.getExitTime() == null)
                .collect(Collectors.toList());
        List<ParkingSlot> history = all.stream()
                .filter(s -> s.getExitTime() != null)
                .collect(Collectors.toList());
        model.addAttribute("parking", new ParkingSlot());
        model.addAttribute("list", active);
        model.addAttribute("history", history);
        model.addAttribute("occupied", getOccupiedSlots(all));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        if (session.getAttribute("admin") == null) {
            return "login";
        }

        addModelAttributes(model);

        return "index";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ParkingSlot parkingSlot,
                       HttpSession session,
                       Model model) {

        if (session.getAttribute("admin") == null) {
            return "login";
        }

        try {

            ParkingSlot existingSlot =
                    repository.findBySlotNumber(parkingSlot.getSlotNumber());

            if (existingSlot != null && existingSlot.getExitTime() == null) {

                model.addAttribute("error",
                        "Slot Already Booked!");

                addModelAttributes(model);

                return "index";
            }

            parkingSlot.setEntryTime(LocalDateTime.now());
            repository.save(parkingSlot);

            model.addAttribute("success",
                    "Parking Slot Booked Successfully");

        } catch (Exception e) {

            model.addAttribute("error",
                    "Error : " + e.getMessage());
        }

        addModelAttributes(model);

        return "index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id,
                         HttpSession session,
                         Model model) {

        if (session.getAttribute("admin") == null) {
            return "login";
        }

        try {

            repository.deleteById(id);

            model.addAttribute("success",
                    "Record Deleted Successfully");

        } catch (Exception e) {

            model.addAttribute("error",
                    "Unable To Delete Record");
        }

        addModelAttributes(model);

        return "index";
    }

    @GetMapping("/left/{id}")
    public String left(@PathVariable Integer id,
                       HttpSession session,
                       Model model) {

        if (session.getAttribute("admin") == null) {
            return "login";
        }

        try {

            ParkingSlot slot = repository.findById(id).orElse(null);
            if (slot != null) {
                slot.setExitTime(LocalDateTime.now());
                repository.save(slot);
            }

            model.addAttribute("success",
                    "Vehicle Marked as Left");

        } catch (Exception e) {

            model.addAttribute("error",
                    "Unable To Mark Vehicle as Left");
        }

        addModelAttributes(model);

        return "index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       HttpSession session,
                       Model model) {

        if (session.getAttribute("admin") == null) {
            return "login";
        }

        ParkingSlot parking =
                repository.findById(id).orElse(null);

        model.addAttribute("parking", parking);
        model.addAttribute("list", repository.findAll());

        return "edit";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute ParkingSlot parkingSlot,
                         HttpSession session,
                         Model model) {

        if (session.getAttribute("admin") == null) {
            return "login";
        }

        try {

            ParkingSlot existing = repository.findById(parkingSlot.getId()).orElse(null);
            if (existing != null) {
                parkingSlot.setEntryTime(existing.getEntryTime());
            }

            repository.save(parkingSlot);

            model.addAttribute("success",
                    "Record Updated Successfully");

        } catch (Exception e) {

            model.addAttribute("error",
                    "Unable To Update Record");
        }

        addModelAttributes(model);

        return "index";
    }
}