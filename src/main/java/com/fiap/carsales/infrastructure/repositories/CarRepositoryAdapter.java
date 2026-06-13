package com.fiap.carsales.infrastructure.repositories;

import com.fiap.carsales.domain.entities.Car;
import com.fiap.carsales.domain.enums.CarStatus;
import com.fiap.carsales.domain.repositories.CarRepository;
import com.fiap.carsales.infrastructure.persistence.entities.CarJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CarRepositoryAdapter implements CarRepository {

    private final CarSpringDataRepository repo;

    public CarRepositoryAdapter(CarSpringDataRepository repo) {
        this.repo = repo;
    }

    @Override
    public void add(Car car) {
        repo.save(CarJpaEntity.fromDomain(car));
    }

    @Override
    public void update(Car car) {
        repo.save(CarJpaEntity.fromDomain(car));
    }

    @Override
    public Optional<Car> getById(UUID id) {
        return repo.findById(id.toString()).map(CarJpaEntity::toDomain);
    }

    @Override
    public Optional<Car> getByLicensePlate(String licensePlate) {
        return repo.findByLicensePlate(licensePlate).map(CarJpaEntity::toDomain);
    }

    @Override
    public List<Car> getByStatusOrderedByPriceAsc(CarStatus status) {
        return repo.findByStatusOrderByPriceAsc(status).stream().map(CarJpaEntity::toDomain).toList();
    }
}
