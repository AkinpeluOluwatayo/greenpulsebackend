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
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String deviceId;
    private String message;
    private String severity; // DANGER, WARNING
    private LocalDateTime timestamp;
    private boolean resolved;
}
