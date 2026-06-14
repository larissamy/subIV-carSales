package com.fiap.carsales.application.services;

import com.fiap.carsales.application.dto.request.RegisterCarRequest;
import com.fiap.carsales.application.dto.request.UpdateCarRequest;
import com.fiap.carsales.application.exceptions.BusinessException;
import com.fiap.carsales.application.exceptions.NotFoundException;
import com.fiap.carsales.domain.entities.Car;
import com.fiap.carsales.domain.enums.CarStatus;
import com.fiap.carsales.domain.repositories.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarServiceTest {

    private CarRepository repository;
    private CarService service;

    @BeforeEach
    void setup() {
        repository = mock(CarRepository.class);
        service = new CarService(repository);
    }

    @Test
    void shouldRegisterCar() {
        var request = new RegisterCarRequest("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        when(repository.getByLicensePlate("ABC1D23")).thenReturn(Optional.empty());

        service.registerCar(request);

        verify(repository).add(any(Car.class));
    }

    @Test
    void shouldNotRegisterDuplicatedLicensePlate() {
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        var request = new RegisterCarRequest("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        when(repository.getByLicensePlate("ABC1D23")).thenReturn(Optional.of(car));

        assertThrows(BusinessException.class, () -> service.registerCar(request));
        verify(repository, never()).add(any());
    }

    @Test
    void shouldUpdateCar() {
        var id = UUID.randomUUID();
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        when(repository.getById(id)).thenReturn(Optional.of(car));
        when(repository.getByLicensePlate("XYZ9A88")).thenReturn(Optional.empty());

        var response = service.updateCar(id, new UpdateCarRequest("Honda", "Civic", 2024, "Preto", BigDecimal.valueOf(140000), "XYZ9A88"));

        assertEquals("Honda", response.brand());
        assertEquals("XYZ9A88", response.licensePlate());
        verify(repository).update(car);
    }

    @Test
    void shouldReturnAvailableCarsOrderedByPrice() {
        var car = Car.create("Fiat", "Argo", 2022, "Branco", "AAA1A11", BigDecimal.valueOf(70000));
        when(repository.getByStatusOrderedByPriceAsc(CarStatus.AVAILABLE)).thenReturn(List.of(car));

        var response = service.getCarsSale();

        assertEquals(1, response.size());
        assertEquals("AVAILABLE", response.get(0).status());
    }

    @Test
    void shouldReserveCar() {
        var id = UUID.randomUUID();
        var car = Car.create("Jeep", "Compass", 2021, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));
        when(repository.getById(id)).thenReturn(Optional.of(car));

        var response = service.reserveCar(id);

        assertEquals("RESERVED", response.status());
        verify(repository).update(car);
    }

    @Test
    void shouldMarkCarAsSold() {
        var id = UUID.randomUUID();
        var car = Car.create("Jeep", "Compass", 2021, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));
        car.reserve();
        when(repository.getById(id)).thenReturn(Optional.of(car));

        var response = service.markCarAsSold(id);

        assertEquals("SOLD", response.status());
        verify(repository).update(car);
    }

    @Test
    void shouldMakeReservedCarAvailable() {
        var id = UUID.randomUUID();
        var car = Car.create("Jeep", "Compass", 2021, "Cinza", "BBB2B22", BigDecimal.valueOf(130000));
        car.reserve();
        when(repository.getById(id)).thenReturn(Optional.of(car));

        var response = service.makeCarAvailable(id);

        assertEquals("AVAILABLE", response.status());
        verify(repository).update(car);
    }

    @Test
    void shouldThrowNotFoundWhenCarDoesNotExist() {
        var id = UUID.randomUUID();
        when(repository.getById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getCarById(id));
    }
}
