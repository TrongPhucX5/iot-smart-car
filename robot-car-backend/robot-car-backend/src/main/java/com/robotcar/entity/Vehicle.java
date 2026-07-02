package com.robotcar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_name", nullable = false, length = 100)
    private String vehicleName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ONLINE','OFFLINE') DEFAULT 'OFFLINE'")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_mode", columnDefinition = "ENUM('MANUAL','LINE_TRACKING','OBSTACLE_AVOIDANCE','LIGHT_SEEKING','FOLLOW_ME','GRAVITY_CONTROL') DEFAULT 'MANUAL'")
    private Mode currentMode;

    @ManyToOne
    @JoinColumn(name = "user_id") // Khớp với tên cột trong SQL
    private User owner;

    @Column(name = "wifi_signal", length = 50)
    private String wifiSignal;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        ONLINE, OFFLINE
    }

    public enum Mode {
        MANUAL, LINE_TRACKING, OBSTACLE_AVOIDANCE, LIGHT_SEEKING, FOLLOW_ME, GRAVITY_CONTROL
    }
}
