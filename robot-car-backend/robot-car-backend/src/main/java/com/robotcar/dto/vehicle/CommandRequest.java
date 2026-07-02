package com.robotcar.dto.vehicle;

import lombok.Data;

@Data
public class CommandRequest {

    private Long vehicleId;

    private String command;

}