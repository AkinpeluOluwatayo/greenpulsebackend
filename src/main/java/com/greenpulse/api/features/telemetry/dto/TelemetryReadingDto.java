package com.greenpulse.api.features.telemetry.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReadingDto {
    private String deviceId;
    private Double temperature;
    private Double humidity;
    private Double soilMoisture;
    private LocalDateTime timestamp;
    private String status;
}
