package com.fiap.carsales.application.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RegisterCarRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @Min(1900) int year,
        @NotBlank String color,
        @NotBlank String licensePlate,
        @DecimalMin(value = "0.01") BigDecimal price
) {}
