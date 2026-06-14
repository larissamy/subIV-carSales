package com.fiap.carsales.application.interfaces;

import com.fiap.carsales.application.dto.request.RegisterCarRequest;
import com.fiap.carsales.application.dto.request.UpdateCarRequest;
import com.fiap.carsales.application.dto.response.CarResponse;

import java.util.List;
import java.util.UUID;

public interface CarServicePort {
    List<CarResponse> getCarsSale();
    List<CarResponse> getCarsSold();
    CarResponse getCarById(UUID id);
    void registerCar(RegisterCarRequest request);
    CarResponse updateCar(UUID id, UpdateCarRequest request);
    CarResponse reserveCar(UUID id);
    CarResponse markCarAsSold(UUID id);
    CarResponse makeCarAvailable(UUID id);
}
