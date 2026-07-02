package com.robotcar.dto.sensor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensorResponse {

    private Double distance;

    private Double lightLevel;

    private String lineStatus;

    private String wifiSignal;

    private LocalDateTime timestamp;

}