package com.parkease.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.parkease.model.VehicleRegistration;
import com.parkease.repository.ParkingRepository;
import com.parkease.repository.VehicleRegistrationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Controller
public class QRCodeController {

    @Autowired
    private VehicleRegistrationRepository vehicleRepo;

    @Autowired
    private ParkingRepository parkingRepo;

    @GetMapping("/qrcode")
    public String qrCodePage(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "login";
        }
        model.addAttribute("registrations", vehicleRepo.findAll());
        return "qrcode";
    }

    @PostMapping("/qrcode/generate")
    public ResponseEntity<?> generateQR(@RequestParam String vehicleNumber,
                                         @RequestParam String ownerName,
                                         @RequestParam String vehicleType,
                                         @RequestParam String mobileNumber,
                                         HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return ResponseEntity.status(401).build();
        }

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
    public String scanQR(@RequestParam String vehicleNumber, HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "login";
        }

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
    public List<VehicleRegistration> listRegistrations() {
        return vehicleRepo.findAll();
    }
}
