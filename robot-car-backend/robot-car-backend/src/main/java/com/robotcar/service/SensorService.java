package com.robotcar.service;

import com.robotcar.dto.sensor.SensorResponse;

public interface SensorService {

    SensorResponse getLatestSensor(Long vehicleId);

}