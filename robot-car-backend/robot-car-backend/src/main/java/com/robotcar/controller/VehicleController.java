package com.robotcar.controller;

import com.robotcar.dto.vehicle.CommandRequest;
import com.robotcar.dto.vehicle.ModeRequest;
import com.robotcar.entity.Vehicle;
import com.robotcar.repository.VehicleRepository;
import com.robotcar.service.VehicleService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping("/command")
    public com.robotcar.dto.common.SimpleResponse command(
            @RequestBody CommandRequest request) {

        vehicleService.sendCommand(request);

        return new com.robotcar.dto.common.SimpleResponse("Command Sent", "SUCCESS");

    }

    @PostMapping("/mode")
    public com.robotcar.dto.common.SimpleResponse changeMode(
            @RequestBody ModeRequest request) {

        vehicleService.changeMode(request);

        return new com.robotcar.dto.common.SimpleResponse("Mode Updated", "SUCCESS");

    }

    @PostMapping
    public com.robotcar.dto.common.SimpleResponse createVehicle(@RequestBody com.robotcar.dto.vehicle.VehicleCreateRequest request) {
        String msg = vehicleService.createVehicle(request);
        return new com.robotcar.dto.common.SimpleResponse(msg, "SUCCESS");
    }

    @GetMapping
    public java.util.List<com.robotcar.dto.vehicle.VehicleResponse> getVehicles() {
        return vehicleService.getVehicles();
    }

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getVehicles(Principal principal) {
        // 1. Lấy username của người dùng đang đăng nhập từ JWT Token
        String username = principal.getName(); 
        
        // 2. Chỉ tìm những xe thuộc về user này
        List<Vehicle> userVehicles = vehicleRepository.findByOwnerUsername(username);
        
        // 3. Trả dữ liệu về cho App Android
        return ResponseEntity.ok(userVehicles);
    }
}