package com.greenpulse.api.features.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private Double averageTemperature;
    private Double averageHumidity;
    private Double averageSoilMoisture;
    private Long activeDevicesCount;
    private Long openAlertsCount;
}
