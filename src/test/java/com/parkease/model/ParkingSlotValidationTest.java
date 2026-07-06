package com.parkease.model;

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
}
