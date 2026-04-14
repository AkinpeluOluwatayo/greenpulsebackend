package com.greenpulse.api.features.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropRequest {
    private String id;
    private String name;
    private Double tempMin;
    private Double tempMax;
    private Double humidityMin;
    private Double humidityMax;
    private Double soilMoistureMin;
    private Double soilMoistureMax;
}
