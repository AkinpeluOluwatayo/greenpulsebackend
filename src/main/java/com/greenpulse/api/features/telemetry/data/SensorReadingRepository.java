package com.greenpulse.api.features.telemetry.data;

import com.greenpulse.api.features.auth.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findByDeviceIdAndOwnerOrderByTimestampDesc(String deviceId, User owner);

    List<SensorReading> findByOwner(User owner);
}
