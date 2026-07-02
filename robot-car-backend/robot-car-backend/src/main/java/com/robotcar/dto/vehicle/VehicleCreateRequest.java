package com.robotcar.dto.vehicle;

import lombok.Data;

@Data
public class VehicleCreateRequest {
    private String vehicleName;
    private String ipAddress;
}
