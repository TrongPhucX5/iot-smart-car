package com.robotcar.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.robotcar.dto.sensor.SensorResponse;
import com.robotcar.service.SensorService;

@Service
public class SensorServiceImpl
        implements SensorService {

    @Override
    public SensorResponse getLatestSensor(Long vehicleId) {

        return new SensorResponse(
                25.4,
                380.0,
                "CENTER",
                "-55",
                LocalDateTime.now());

    }
}