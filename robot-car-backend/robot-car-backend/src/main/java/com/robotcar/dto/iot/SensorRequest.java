package com.robotcar.dto.iot;

import lombok.Data;

@Data
public class SensorRequest {

    private Long vehicleId;

    private Double distance;

    private Double lightLevel;

    private String lineStatus;

    private String wifiSignal;

    private String currentMode;

}