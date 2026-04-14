package com.greenpulse.api.features.telemetry.data;

import com.greenpulse.api.features.auth.data.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sensor_readings", indexes = {
        @Index(name = "idx_reading_time", columnList = "timestamp"),
        @Index(name = "idx_device_id", columnList = "deviceId")
})
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String deviceId;
    private Double temperature;
    private Double humidity;
    private Double soilMoisture;
    private Double vpd;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        OPTIMAL, WARNING, DANGER
    }
}
