package com.greenpulse.api.features.telemetry.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropThresholdRequest {
    private String profileId;
    private Double tempMin;
    private Double tempMax;
    private Double humidityMin;
    private Double humidityMax;
    private Double soilMoistureMin;
    private Double soilMoistureMax;
}
