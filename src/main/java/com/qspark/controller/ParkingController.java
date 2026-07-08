/*
 * QSpark - A production-grade smart parking management system
 * Copyright (C) 2026 PawanSimha
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

import com.qspark.model.ParkingSlot;
import com.qspark.repository.ParkingRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ParkingController {

    private final ParkingRepository repository;

    public ParkingController(ParkingRepository repository) {
        this.repository = repository;
    }

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
    public String dashboard(Model model,
                            @RequestParam(required = false) String ownerName,
                            @RequestParam(required = false) String vehicleNumber,
                            @RequestParam(required = false) String vehicleType,
                            @RequestParam(required = false) String mobileNumber) {

        addModelAttributes(model);

        if (vehicleNumber != null && !vehicleNumber.isEmpty()) {
            model.addAttribute("scannedOwnerName", ownerName != null ? ownerName : "");
            model.addAttribute("scannedVehicleNumber", vehicleNumber);
            model.addAttribute("scannedVehicleType", vehicleType != null ? vehicleType : "");
            model.addAttribute("scannedMobileNumber", mobileNumber != null ? mobileNumber : "");
        }

        return "index";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute ParkingSlot parkingSlot,
                       BindingResult result,
                       Model model) {

        if (result.hasErrors()) {
            model.addAttribute("error",
                    "Invalid vehicle number format (use: KA 01 AB 1234 or 26 BH 9876 AB)");
            if (result.hasFieldErrors("vehicleNumber")) {
                model.addAttribute("vehicleNumberError",
                        result.getFieldError("vehicleNumber").getDefaultMessage());
            }
            addModelAttributes(model);
            return "index";
        }

        try {

            ParkingSlot existingSlot =
                    repository.findBySlotNumber(parkingSlot.getSlotNumber());

            if (existingSlot != null && existingSlot.getExitTime() == null) {
                model.addAttribute("error", "Slot Already Booked!");
                addModelAttributes(model);
                return "index";
            }

            if (repository.existsByVehicleNumberAndExitTimeIsNull(parkingSlot.getVehicleNumber())) {
                model.addAttribute("error", "Vehicle already parked in another slot!");
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
                         Model model) {

        try {

            if (!repository.existsById(id)) {
                model.addAttribute("error", "Record not found");
                addModelAttributes(model);
                return "index";
            }

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
                       Model model) {

        try {

            ParkingSlot slot = repository.findById(id).orElse(null);
            if (slot == null) {
                model.addAttribute("error", "Record not found");
                addModelAttributes(model);
                return "index";
            }

            slot.setExitTime(LocalDateTime.now());
            repository.save(slot);

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
                       Model model) {

        ParkingSlot parking =
                repository.findById(id).orElse(null);

        if (parking == null) {
            model.addAttribute("error", "Record not found");
            addModelAttributes(model);
            return "index";
        }

        model.addAttribute("parking", parking);
        model.addAttribute("list", repository.findAll());

        return "edit";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute ParkingSlot parkingSlot,
                         BindingResult result,
                         Model model) {

        if (result.hasErrors()) {
            model.addAttribute("error",
                    "Invalid vehicle number format (use: KA 01 AB 1234 or 26 BH 9876 AB)");
            if (result.hasFieldErrors("vehicleNumber")) {
                model.addAttribute("vehicleNumberError",
                        result.getFieldError("vehicleNumber").getDefaultMessage());
            }
            addModelAttributes(model);
            return "index";
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
