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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qspark.model.VehicleRegistration;
import com.qspark.repository.ParkingRepository;
import com.qspark.repository.VehicleRegistrationRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Controller
public class QRCodeController {

    private final VehicleRegistrationRepository vehicleRepo;
    private final ParkingRepository parkingRepo;

    public QRCodeController(VehicleRegistrationRepository vehicleRepo, ParkingRepository parkingRepo) {
        this.vehicleRepo = vehicleRepo;
        this.parkingRepo = parkingRepo;
    }

    @GetMapping("/qrcode")
    public String qrCodePage(Model model) {
        model.addAttribute("registrations", vehicleRepo.findAll());
        return "qrcode";
    }

    @PostMapping("/qrcode/generate")
    public ResponseEntity<?> generateQR(@RequestParam String vehicleNumber,
                                         @RequestParam String ownerName,
                                         @RequestParam String vehicleType,
                                         @RequestParam String mobileNumber) {

        String formatted = vehicleNumber.toUpperCase().trim();

        VehicleRegistration reg = vehicleRepo.findByVehicleNumber(formatted).orElse(null);
        if (reg != null) {
            reg.setOwnerName(ownerName.trim());
            reg.setVehicleType(vehicleType);
            reg.setMobileNumber(mobileNumber.trim());
            vehicleRepo.save(reg);
        } else {
            vehicleRepo.save(new VehicleRegistration(formatted, ownerName.trim(), vehicleType, mobileNumber.trim()));
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(formatted, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "attachment; filename=\"QR-" + formatted.replace(" ", "_") + ".png\"")
                    .body(out.toByteArray());
        } catch (WriterException e) {
            return ResponseEntity.badRequest().body("Error generating QR code");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating QR code");
        }
    }

    @PostMapping("/qrcode/scan")
    public String scanQR(@RequestParam String vehicleNumber, Model model) {

        String formatted = vehicleNumber.toUpperCase().trim();

        VehicleRegistration reg = vehicleRepo.findByVehicleNumber(formatted).orElse(null);
        if (reg == null) {
            model.addAttribute("error", "Vehicle not registered. Generate QR first.");
            model.addAttribute("registrations", vehicleRepo.findAll());
            return "qrcode";
        }

        return "redirect:/dashboard?ownerName=" + urlEncode(reg.getOwnerName())
                + "&vehicleNumber=" + urlEncode(reg.getVehicleNumber())
                + "&vehicleType=" + urlEncode(reg.getVehicleType())
                + "&mobileNumber=" + urlEncode(reg.getMobileNumber());
    }

    private String urlEncode(String s) {
        if (s == null) return "";
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    @GetMapping("/qrcode/list")
    @ResponseBody
    public ResponseEntity<?> listRegistrations() {
        return ResponseEntity.ok(vehicleRepo.findAll());
    }
}
