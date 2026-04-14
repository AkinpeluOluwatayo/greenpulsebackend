package com.greenpulse.api.features.devices.data;

import com.greenpulse.api.features.auth.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByOwner(User owner);

    Optional<Device> findByDeviceIdAndOwner(String deviceId, User owner);
}
