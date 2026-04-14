package com.greenpulse.api.features.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private Long id;
    private String deviceId;
    private String message;
    private String severity;
    private LocalDateTime timestamp;
    private boolean resolved;
}
