package com.robotcar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotcar.entity.VehicleLog;

public interface VehicleLogRepository
        extends JpaRepository<VehicleLog, Long> {

}