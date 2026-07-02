package com.robotcar.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long vehicleId;
    private String vehicleName;
    private String status;
    private String currentMode;
    private String wifiSignal;
}
