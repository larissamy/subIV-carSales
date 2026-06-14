package com.fiap.carsales.application.services;

import com.fiap.carsales.application.dto.request.RegisterCarRequest;
import com.fiap.carsales.application.dto.request.UpdateCarRequest;
import com.fiap.carsales.application.dto.response.CarResponse;
import com.fiap.carsales.application.exceptions.BusinessException;
import com.fiap.carsales.application.exceptions.NotFoundException;
import com.fiap.carsales.application.interfaces.CarServicePort;
import com.fiap.carsales.domain.entities.Car;
import com.fiap.carsales.domain.enums.CarStatus;
import com.fiap.carsales.domain.repositories.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CarService implements CarServicePort {

    private final CarRepository repository;

    public CarService(CarRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CarResponse> getCarsSale() {
        var cars = repository.getByStatusOrderedByPriceAsc(CarStatus.AVAILABLE);
        return cars.stream().map(this::toResponse).toList();
    }

    @Override
    public List<CarResponse> getCarsSold() {
        var cars = repository.getByStatusOrderedByPriceAsc(CarStatus.SOLD);
        return cars.stream().map(this::toResponse).toList();
    }

    @Override
    public CarResponse getCarById(UUID id) {
        var car = getRequiredCar(id);
        return toResponse(car);
    }

    @Override
    public void registerCar(RegisterCarRequest request) {
        var existing = repository.getByLicensePlate(request.licensePlate());
        if (existing.isPresent()) {
            throw new BusinessException("Placa já registrada: " + request.licensePlate());
        }

        var car = Car.create(
                request.brand(),
                request.model(),
                request.year(),
                request.color(),
                request.licensePlate(),
                request.price()
        );
        repository.add(car);
    }

    @Override
    public CarResponse updateCar(UUID id, UpdateCarRequest request) {
        var car = getRequiredCar(id);

        var brand = request.brand() != null ? request.brand() : car.getBrand();
        var model = request.model() != null ? request.model() : car.getModel();
        var year = request.year() != null ? request.year() : car.getYear();
        var color = request.color() != null ? request.color() : car.getColor();
        var price = request.price() != null ? request.price() : car.getPrice();
        var licensePlate = request.licensePlate() != null ? request.licensePlate() : car.getLicensePlate();

        repository.getByLicensePlate(licensePlate)
                .filter(existing -> !existing.getId().equals(car.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException("Placa já registrada: " + licensePlate);
                });

        car.updateDetails(brand, model, year, color, price, licensePlate);
        repository.update(car);
        return toResponse(car);
    }

    @Override
    public CarResponse reserveCar(UUID id) {
        var car = getRequiredCar(id);
        car.reserve();
        repository.update(car);
        return toResponse(car);
    }

    @Override
    public CarResponse markCarAsSold(UUID id) {
        var car = getRequiredCar(id);
        car.markAsSold();
        repository.update(car);
        return toResponse(car);
    }

    @Override
    public CarResponse makeCarAvailable(UUID id) {
        var car = getRequiredCar(id);
        car.makeAvailable();
        repository.update(car);
        return toResponse(car);
    }

    private Car getRequiredCar(UUID id) {
        return repository.getById(id).orElseThrow(() -> new NotFoundException("Carro não encontrado."));
    }

    private CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getColor(),
                car.getLicensePlate(),
                car.getPrice(),
                car.getStatus().name(),
                car.getUpdatedAt()
        );
    }
}
