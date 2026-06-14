package com.fiap.carsales.presentation.controllers;

import com.fiap.carsales.application.dto.request.RegisterCarRequest;
import com.fiap.carsales.application.dto.request.UpdateCarRequest;
import com.fiap.carsales.application.interfaces.CarServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Cars")
@RestController
@RequestMapping("/api/cars")
public class CarsController {

    private final CarServicePort service;

    public CarsController(CarServicePort service) {
        this.service = service;
    }

    @Operation(summary = "Get car by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getCarById(id));
    }

    @Operation(summary = "Register a new car for sale")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RegisterCarRequest request) {
        service.registerCar(request);
        return ResponseEntity.created(URI.create("/api/cars")).build();
    }

    @Operation(summary = "Update a car data (cannot update sold cars)")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody UpdateCarRequest request) {
        return ResponseEntity.ok(service.updateCar(id, request));
    }
}
