package com.robotcar.controller;

import com.robotcar.dto.iot.CommandResponse;
import com.robotcar.dto.iot.SensorRequest;
import com.robotcar.service.IotService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
public class IotController {

    private final IotService iotService;

    @PostMapping("/sensor")
    public String sensor(
            @RequestBody SensorRequest request) {

        iotService.saveSensorData(request);

        return "Sensor Saved";

    }

    @GetMapping("/command")
    public String getCommand(
            @RequestParam Long vehicleId) {

        return iotService
                .getLatestCommand(vehicleId);

    }
}