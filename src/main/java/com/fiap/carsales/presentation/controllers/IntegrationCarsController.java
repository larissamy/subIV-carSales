package com.fiap.carsales.presentation.controllers;

import com.fiap.carsales.application.interfaces.CarServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Integration Cars")
@RestController
@RequestMapping("/api/integration/cars")
public class IntegrationCarsController {

    private final CarServicePort service;

    public IntegrationCarsController(CarServicePort service) {
        this.service = service;
    }

    @Operation(summary = "Internal endpoint: list available cars ordered by price (asc)")
    @GetMapping("/available")
    public ResponseEntity<?> getAllAvailable() {
        return ResponseEntity.ok(service.getCarsSale());
    }

    @Operation(summary = "Internal endpoint: list sold cars ordered by price (asc)")
    @GetMapping("/sold")
    public ResponseEntity<?> getAllSold() {
        return ResponseEntity.ok(service.getCarsSold());
    }

    @Operation(summary = "Internal endpoint: get car by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getCarById(id));
    }

    @Operation(summary = "Internal endpoint: reserve a car")
    @PatchMapping("/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.reserveCar(id));
    }

    @Operation(summary = "Internal endpoint: mark car as sold")
    @PatchMapping("/{id}/sold")
    public ResponseEntity<?> sold(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.markCarAsSold(id));
    }

    @Operation(summary = "Internal endpoint: make reserved car available again")
    @PatchMapping("/{id}/available")
    public ResponseEntity<?> available(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.makeCarAvailable(id));
    }
}
