package com.robotcar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotcar.entity.VehicleCommand;

public interface VehicleCommandRepository
        extends JpaRepository<VehicleCommand, Long> {

    VehicleCommand findTopByOrderByCreatedAtDesc();

    VehicleCommand findFirstByVehicle_VehicleIdAndCommandStatusOrderByCreatedAtAsc(Long vehicleId, VehicleCommand.CommandStatus status);

}