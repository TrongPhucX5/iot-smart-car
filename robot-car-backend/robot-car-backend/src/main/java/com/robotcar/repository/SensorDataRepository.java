package com.robotcar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotcar.entity.SensorData;

public interface SensorDataRepository
        extends JpaRepository<SensorData, Long> {

    SensorData findTopByOrderByCreatedAtDesc();

}