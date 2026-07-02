package com.robotcar.controller;

import com.robotcar.dto.sensor.SensorResponse;
import com.robotcar.service.SensorService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping("/latest")
    public SensorResponse latest(@RequestParam Long vehicleId) {

        return sensorService.getLatestSensor(vehicleId);

    }
}