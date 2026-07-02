package com.robotcar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Long sensorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private Double distance;

    @Column(name = "light_level")
    private Double lightLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_status", columnDefinition = "ENUM('LEFT','CENTER','RIGHT','LOST')")
    private LineStatus lineStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum LineStatus {
        LEFT, CENTER, RIGHT, LOST
    }
}
