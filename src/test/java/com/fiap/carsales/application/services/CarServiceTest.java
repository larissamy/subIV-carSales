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
        assertEquals("Civic", response.model());
        assertEquals(2024, response.year());
        assertEquals("Preto", response.color());
        assertEquals(BigDecimal.valueOf(140000), response.price());
        assertEquals("XYZ9A88", response.licensePlate());
        verify(repository).update(car);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        var id = UUID.randomUUID();
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        when(repository.getById(id)).thenReturn(Optional.of(car));
        when(repository.getByLicensePlate("ABC1D23")).thenReturn(Optional.of(car));

        var response = service.updateCar(id, new UpdateCarRequest(null, "Cross", null, null, BigDecimal.valueOf(125000), null));

        assertEquals("Toyota", response.brand());
        assertEquals("Cross", response.model());
        assertEquals(2023, response.year());
        assertEquals("Prata", response.color());
        assertEquals(BigDecimal.valueOf(125000), response.price());
        assertEquals("ABC1D23", response.licensePlate());
        verify(repository).update(car);
    }

    @Test
    void shouldNotUpdateCarWithDuplicatedLicensePlateFromAnotherCar() {
        var id = UUID.randomUUID();
        var car = Car.create("Toyota", "Corolla", 2023, "Prata", "ABC1D23", BigDecimal.valueOf(120000));
        var anotherCar = Car.create("Honda", "Civic", 2022, "Preto", "XYZ9A88", BigDecimal.valueOf(110000));
        when(repository.getById(id)).thenReturn(Optional.of(car));
        when(repository.getByLicensePlate("XYZ9A88")).thenReturn(Optional.of(anotherCar));

        assertThrows(BusinessException.class, () -> service.updateCar(id, new UpdateCarRequest(null, null, null, null, null, "XYZ9A88")));
        verify(repository, never()).update(any());
    }

    @Test
    void shouldReturnAvailableCarsOrderedByPrice() {
        var car = Car.create("Fiat", "Argo", 2022, "Branco", "AAA1A11", BigDecimal.valueOf(70000));
        when(repository.getByStatusOrderedByPriceAsc(CarStatus.AVAILABLE)).thenReturn(List.of(car));

        var response = service.getCarsSale();

        assertEquals(1, response.size());
        assertEquals("AVAILABLE", response.get(0).status());
        assertEquals("Fiat", response.get(0).brand());
    }

    @Test
    void shouldReturnSoldCarsOrderedByPrice() {
        var car = Car.create("Ford", "Ka", 2020, "Vermelho", "CCC3C33", BigDecimal.valueOf(50000));
        car.markAsSold();
        when(repository.getByStatusOrderedByPriceAsc(CarStatus.SOLD)).thenReturn(List.of(car));

        var response = service.getCarsSold();

        assertEquals(1, response.size());
        assertEquals("SOLD", response.get(0).status());
        assertEquals("Ford", response.get(0).brand());
        verify(repository).getByStatusOrderedByPriceAsc(CarStatus.SOLD);
    }

    @Test
    void shouldGetCarById() {
        var id = UUID.randomUUID();
        var car = Car.create("Volkswagen", "T-Cross", 2024, "Azul", "DDD4D44", BigDecimal.valueOf(150000));
        when(repository.getById(id)).thenReturn(Optional.of(car));

        var response = service.getCarById(id);

        assertEquals(car.getId(), response.id());
        assertEquals("Volkswagen", response.brand());
        assertEquals("T-Cross", response.model());
        assertEquals("AVAILABLE", response.status());
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

    @Test
    void shouldThrowNotFoundWhenUpdatingMissingCar() {
        var id = UUID.randomUUID();
        when(repository.getById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateCar(id, new UpdateCarRequest("Honda", null, null, null, null, null)));
        verify(repository, never()).update(any());
    }
}
