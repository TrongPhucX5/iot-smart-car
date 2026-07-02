package com.robotcar.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.robotcar.dto.vehicle.CommandRequest;
import com.robotcar.dto.vehicle.ModeRequest;
import com.robotcar.entity.User;
import com.robotcar.entity.Vehicle;
import com.robotcar.entity.VehicleCommand;
import com.robotcar.repository.UserRepository;
import com.robotcar.repository.VehicleCommandRepository;
import com.robotcar.repository.VehicleRepository;
import com.robotcar.service.VehicleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl
        implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCommandRepository commandRepository;
    private final UserRepository userRepository;

    @Override
    public void sendCommand(CommandRequest request) {

        Vehicle vehicle =
                vehicleRepository.findById(
                        request.getVehicleId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found"));

        User user =
                userRepository.findById(1L)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"));

        VehicleCommand command =
                new VehicleCommand();

        command.setVehicle(vehicle);

        command.setUser(user);

        command.setCommand(
                VehicleCommand.Command.valueOf(
                        request.getCommand()));

        command.setCommandStatus(
                VehicleCommand.CommandStatus.PENDING);

        command.setCreatedAt(
                LocalDateTime.now());

        commandRepository.save(command);
    }

    @Override
    public void changeMode(
            ModeRequest request) {

        Vehicle vehicle =
                vehicleRepository.findById(
                        request.getVehicleId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found"));

        vehicle.setCurrentMode(
                Vehicle.Mode.valueOf(
                        request.getMode()));

        vehicleRepository.save(vehicle);
    }

    @Override
    public String createVehicle(com.robotcar.dto.vehicle.VehicleCreateRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleName(request.getVehicleName() != null ? request.getVehicleName() : "Robot Car");
        vehicle.setIpAddress(request.getIpAddress());
        vehicle.setStatus(Vehicle.Status.OFFLINE);
        vehicle.setCurrentMode(Vehicle.Mode.MANUAL);
        
        vehicle = vehicleRepository.save(vehicle);
        return "Vehicle created with ID: " + vehicle.getVehicleId();
    }

    @Override
    public java.util.List<com.robotcar.dto.vehicle.VehicleResponse> getVehicles() {
        return vehicleRepository.findAll().stream().map(v -> new com.robotcar.dto.vehicle.VehicleResponse(
                v.getVehicleId(),
                v.getVehicleName(),
                v.getStatus().name(),
                v.getCurrentMode().name(),
                v.getWifiSignal()
        )).collect(java.util.stream.Collectors.toList());
    }
}