package com.fiap.carsales.infrastructure.persistence.entities;

import com.fiap.carsales.domain.entities.Car;
import com.fiap.carsales.domain.enums.CarStatus;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Keeps domain entities framework-free by rehydrating with reflection.
 * (Alternative would be package-private constructors in domain.)
 */
final class CarMapper {
    private CarMapper() {}

    static Car rehydrate(UUID id, String brand, String model, int year, String color, String licensePlate, BigDecimal price, CarStatus status, Instant updatedAt) {
        try {
            Constructor<Car> c = Car.class.getDeclaredConstructor(UUID.class, String.class, String.class, int.class, String.class, String.class, BigDecimal.class, CarStatus.class, Instant.class);
            c.setAccessible(true);
            return c.newInstance(id, brand, model, year, color, licensePlate, price, status, updatedAt);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rehydrate Car", e);
        }
    }
}
