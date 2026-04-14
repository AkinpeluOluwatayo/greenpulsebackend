package com.greenpulse.api.features.telemetry.data;

import com.greenpulse.api.features.auth.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByDeviceIdAndOwnerAndResolvedFalse(String deviceId, User owner);

    List<Alert> findByOwner(User owner);

    Optional<Alert> findByIdAndOwner(Long id, User owner);
}
