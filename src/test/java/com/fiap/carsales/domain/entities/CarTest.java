package com.fiap.carsales.domain.entities;

import com.fiap.carsales.domain.enums.CarStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CarTest {

    @Test
    void shouldCreateAvailableCar() {
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));

        assertNotNull(car.getId());
        assertEquals("Toyota", car.getBrand());
        assertEquals("Corolla", car.getModel());
        assertEquals(2023, car.getYear());
        assertEquals("Prata", car.getColor());
        assertEquals("ABC1D23", car.getLicensePlate());
        assertEquals(BigDecimal.valueOf(120000), car.getPrice());
        assertEquals(CarStatus.AVAILABLE, car.getStatus());
        assertNotNull(car.getUpdatedAt());
    }

    @Test
    void shouldUpdateAvailableCarDetails() {
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        var originalUpdateDate = car.getUpdatedAt();

        car.updateDetails("Honda", "Civic", 2024, "Preto", BigDecimal.valueOf(140000), "XYZ9A88");

        assertEquals("Honda", car.getBrand());
        assertEquals("Civic", car.getModel());
        assertEquals(2024, car.getYear());
        assertEquals("Preto", car.getColor());
        assertEquals(BigDecimal.valueOf(140000), car.getPrice());
        assertEquals("XYZ9A88", car.getLicensePlate());
        assertTrue(!car.getUpdatedAt().isBefore(originalUpdateDate));
    }

    @Test
    void shouldNotUpdateSoldCar() {
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        car.markAsSold();

        assertThrows(IllegalStateException.class, () -> car.updateDetails("Honda", "Civic", 2024, "Preto", BigDecimal.valueOf(140000), "XYZ9A88"));
    }

    @Test
    void shouldReserveAvailableCar() {
        var car = Car.create("Jeep", "Compass", 2022, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));

        car.reserve();

        assertEquals(CarStatus.RESERVED, car.getStatus());
    }

    @Test
    void shouldNotReserveUnavailableCar() {
        var car = Car.create("Jeep", "Compass", 2022, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));
        car.reserve();

        assertThrows(IllegalStateException.class, car::reserve);
    }

    @Test
    void shouldMakeReservedCarAvailable() {
        var car = Car.create("Jeep", "Compass", 2022, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));
        car.reserve();

        car.makeAvailable();

        assertEquals(CarStatus.AVAILABLE, car.getStatus());
    }

    @Test
    void shouldNotMakeAvailableWhenAlreadyAvailable() {
        var car = Car.create("Jeep", "Compass", 2022, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));

        assertThrows(IllegalStateException.class, car::makeAvailable);
    }

    @Test
    void shouldNotMakeAvailableWhenSold() {
        var car = Car.create("Jeep", "Compass", 2022, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));
        car.markAsSold();

        assertThrows(IllegalStateException.class, car::makeAvailable);
    }

    @Test
    void shouldMarkAvailableCarAsSold() {
        var car = Car.create("Fiat", "Argo", 2020, "Branco", "CCC3C33", BigDecimal.valueOf(65000));

        car.markAsSold();

        assertEquals(CarStatus.SOLD, car.getStatus());
    }

    @Test
    void shouldMarkReservedCarAsSold() {
        var car = Car.create("Fiat", "Argo", 2020, "Branco", "CCC3C33", BigDecimal.valueOf(65000));
        car.reserve();

        car.markAsSold();

        assertEquals(CarStatus.SOLD, car.getStatus());
    }

    @Test
    void shouldNotMarkSoldCarAsSoldAgain() {
        var car = Car.create("Fiat", "Argo", 2020, "Branco", "CCC3C33", BigDecimal.valueOf(65000));
        car.markAsSold();

        assertThrows(IllegalStateException.class, car::markAsSold);
    }

    @Test
    void shouldNotMarkCarAsSoldWhenStatusIsInvalid() {
        var car = Car.create("Fiat", "Argo", 2020, "Branco", "CCC3C33", BigDecimal.valueOf(65000));
        car.setStatus(null);

        assertThrows(IllegalStateException.class, car::markAsSold);
    }

    @Test
    void shouldUpdateStatusAndUpdatedAtThroughPersistenceHelpers() {
        var car = Car.create("Fiat", "Argo", 2020, "Branco", "CCC3C33", BigDecimal.valueOf(65000));
        var date = Instant.parse("2026-01-01T10:00:00Z");

        car.setStatus(CarStatus.RESERVED);
        car.setUpdatedAt(date);

        assertEquals(CarStatus.RESERVED, car.getStatus());
        assertEquals(date, car.getUpdatedAt());
    }

    @Test
    void shouldRequireMandatoryFields() {
        assertThrows(NullPointerException.class, () -> Car.create(null, "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000)));
        assertThrows(NullPointerException.class, () -> Car.create("Toyota", null, 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000)));
        assertThrows(NullPointerException.class, () -> Car.create("Toyota", "Corolla", 2023, null, "ABC1D23", BigDecimal.valueOf(120000)));
        assertThrows(NullPointerException.class, () -> Car.create("Toyota", "Corolla", 2023, "Prata", null, BigDecimal.valueOf(120000)));
        assertThrows(NullPointerException.class, () -> Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", null));
    }
}
