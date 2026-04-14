package com.greenpulse.api.features.devices.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationRequest {
    private String deviceId;
    private String name;
    private String type;
    private String location;
    private String cropId;
}
