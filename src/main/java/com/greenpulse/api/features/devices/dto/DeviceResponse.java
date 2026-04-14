package com.greenpulse.api.features.devices.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {
    private String deviceId;
    private String name;
    private String type;
    private String location;
    private String status; // ONLINE, DELAYED, OFFLINE
    private LocalDateTime lastSeenAt;
}
