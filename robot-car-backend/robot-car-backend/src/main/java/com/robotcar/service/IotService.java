package com.robotcar.service;

import com.robotcar.dto.iot.CommandResponse;
import com.robotcar.dto.iot.SensorRequest;

public interface IotService {

    void saveSensorData(
            SensorRequest request);

    String getLatestCommand(
            Long vehicleId);

}