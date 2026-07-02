package com.robotcar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_id")
    private Long commandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('FORWARD','BACKWARD','LEFT','RIGHT','STOP','AUTO_MODE','LINE_TRACKING','LIGHT_SEEKING','FOLLOW_ME','GRAVITY_CONTROL')")
    private Command command;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_status", columnDefinition = "ENUM('PENDING','SENT','EXECUTED','FAILED') DEFAULT 'PENDING'")
    private CommandStatus commandStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Command {
        FORWARD, BACKWARD, LEFT, RIGHT, STOP, AUTO_MODE, LINE_TRACKING, LIGHT_SEEKING, FOLLOW_ME, GRAVITY_CONTROL
    }

    public enum CommandStatus {
        PENDING, SENT, EXECUTED, FAILED
    }
}
