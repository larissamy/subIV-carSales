package com.fiap.carsales.application.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateCarRequest(
        String brand,
        String model,
        Integer year,
        String color,
        BigDecimal price,
        String licensePlate
) {}