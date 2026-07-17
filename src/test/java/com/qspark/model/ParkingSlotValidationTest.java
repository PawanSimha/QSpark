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

package com.qspark.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingSlotValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validVehicleNumber_ShouldPass() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("KA 01 AB 1234");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void validVehicleNumber_WithSingleLetterSuffix_ShouldPass() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("TN 07 A 1234");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidVehicleNumber_NoSpaces_ShouldFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("KA01AB1234");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidVehicleNumber_TooShort_ShouldFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("KA1A123");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidVehicleNumber_Lowercase_ShouldFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("ka 01 ab 1234");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void nullVehicleNumber_ShouldNotFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber(null);

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void validVehicleNumber_BharatSeries_ShouldPass() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("26 BH 9876 AB");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void validVehicleNumber_ThreeLetterSuffix_ShouldPass() {
        ParkingSlot slot = new ParkingSlot();
        slot.setVehicleNumber("ML 04 ASD 5256");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "vehicleNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void validParkingSlot_ShouldPassAllValidations() {
        ParkingSlot slot = new ParkingSlot();
        slot.setOwnerName("Test User");
        slot.setVehicleNumber("KA 01 AB 1234");
        slot.setVehicleType("Car");
        slot.setSlotNumber("B1-01");
        slot.setMobileNumber("9876543210");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validate(slot);

        assertThat(violations).isEmpty();
    }

    @Test
    void validMobileNumber_ShouldPass() {
        ParkingSlot slot = new ParkingSlot();
        slot.setMobileNumber("9876543210");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "mobileNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void invalidMobileNumber_TooShort_ShouldFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setMobileNumber("12345");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "mobileNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidMobileNumber_WithLetters_ShouldFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setMobileNumber("98765abcde");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "mobileNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void nullMobileNumber_ShouldPass() {
        ParkingSlot slot = new ParkingSlot();
        slot.setMobileNumber(null);

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "mobileNumber");

        assertThat(violations).isEmpty();
    }

    @Test
    void emptyMobileNumber_ShouldFail() {
        ParkingSlot slot = new ParkingSlot();
        slot.setMobileNumber("");

        Set<ConstraintViolation<ParkingSlot>> violations = validator.validateProperty(slot, "mobileNumber");

        assertThat(violations).isNotEmpty();
    }
}
