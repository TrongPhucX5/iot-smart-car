package com.robotcar.service.impl;

import com.robotcar.dto.iot.CommandResponse;
import com.robotcar.dto.iot.SensorRequest;
import com.robotcar.entity.SensorData;
import com.robotcar.entity.Vehicle;
import com.robotcar.entity.VehicleCommand;
import com.robotcar.repository.SensorDataRepository;
import com.robotcar.repository.VehicleCommandRepository;
import com.robotcar.repository.VehicleRepository;
import com.robotcar.service.IotService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IotServiceImpl implements IotService {

    private final SensorDataRepository sensorDataRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCommandRepository vehicleCommandRepository;

    @Override
    public void saveSensorData(SensorRequest request) {

        Vehicle vehicle =
                vehicleRepository.findById(
                        request.getVehicleId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found"));

        vehicle.setWifiSignal(
                request.getWifiSignal());

        vehicleRepository.save(vehicle);

        SensorData sensorData =
                new SensorData();

        sensorData.setVehicle(vehicle);

        sensorData.setDistance(
                request.getDistance());

        sensorData.setLightLevel(
                request.getLightLevel());

        sensorData.setLineStatus(
                SensorData.LineStatus.valueOf(
                        request.getLineStatus()));

        sensorDataRepository.save(sensorData);
    }

    @Override
    public String getLatestCommand(
            Long vehicleId) {

        VehicleCommand command =
                vehicleCommandRepository
                        .findFirstByVehicle_VehicleIdAndCommandStatusOrderByCreatedAtAsc(vehicleId, VehicleCommand.CommandStatus.PENDING);

        if (command == null) {
            return "NONE";
        }

        command.setCommandStatus(VehicleCommand.CommandStatus.EXECUTED);
        vehicleCommandRepository.save(command);

        return command.getCommand().name();
    }
}