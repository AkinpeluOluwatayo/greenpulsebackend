package com.greenpulse.api.features.telemetry.data;

import com.greenpulse.api.features.auth.data.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crop_profiles")
public class CropProfile {

    @Id
    private String id; // e.g., "tomatoes"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    private String name;

    private Double tempMin;
    private Double tempMax;
    private Double tempOptimalMin;
    private Double tempOptimalMax;

    private Double humidityMin;
    private Double humidityMax;
    private Double humidityOptimalMin;
    private Double humidityOptimalMax;

    private Double soilMoistureMin;
    private Double soilMoistureMax;
    private Double soilMoistureOptimalMin;
    private Double soilMoistureOptimalMax;
}
