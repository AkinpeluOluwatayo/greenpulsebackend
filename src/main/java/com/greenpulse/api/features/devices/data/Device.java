package com.greenpulse.api.features.devices.data;

import com.greenpulse.api.features.auth.data.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices")
public class Device {

    @Id
    private String deviceId; // Unique ID from hardware

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    private String type; // e.g., "Soil Probe", "Environment Node"
    private String location; // e.g., "Greenhouse A"

    private String currentCropId; // Linked to CropProfile.id
    private String status; // ONLINE, OFFLINE, DELAYED

    private LocalDateTime lastSeenAt;
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
