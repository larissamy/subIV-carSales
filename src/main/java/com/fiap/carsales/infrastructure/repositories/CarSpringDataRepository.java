package com.fiap.carsales.infrastructure.repositories;

import com.fiap.carsales.domain.enums.CarStatus;
import com.fiap.carsales.infrastructure.persistence.entities.CarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarSpringDataRepository extends JpaRepository<CarJpaEntity, String> {
    Optional<CarJpaEntity> findByLicensePlate(String licensePlate);
    List<CarJpaEntity> findByStatusOrderByPriceAsc(CarStatus status);
}
