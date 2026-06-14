package com.fiap.carsales.domain.repositories;

import com.fiap.carsales.domain.entities.Car;
import com.fiap.carsales.domain.enums.CarStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository {
    void add(Car car);
    void update(Car car);
    Optional<Car> getById(UUID id);
    Optional<Car> getByLicensePlate(String licensePlate);
    List<Car> getByStatusOrderedByPriceAsc(CarStatus status);
}
