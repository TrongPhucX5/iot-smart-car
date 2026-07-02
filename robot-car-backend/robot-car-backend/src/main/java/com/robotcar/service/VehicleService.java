package com.robotcar.service;

import com.robotcar.dto.vehicle.CommandRequest;
import com.robotcar.dto.vehicle.ModeRequest;

public interface VehicleService {

    void sendCommand(CommandRequest request);

    void changeMode(ModeRequest request);

    String createVehicle(com.robotcar.dto.vehicle.VehicleCreateRequest request);

    java.util.List<com.robotcar.dto.vehicle.VehicleResponse> getVehicles();

}