package com.robotcar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotcar.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // Spring Data JPA sẽ tự động sinh câu lệnh SQL: SELECT * FROM vehicles WHERE owner.username = ?
    List<Vehicle> findByOwnerUsername(String username);
}