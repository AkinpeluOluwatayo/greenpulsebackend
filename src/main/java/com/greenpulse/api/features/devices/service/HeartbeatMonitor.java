package com.greenpulse.api.features.devices.service;

import com.greenpulse.api.features.devices.data.Device;
import com.greenpulse.api.features.devices.data.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatMonitor {

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void checkHeartbeats() {
        log.debug("Running heartbeat check...");
        List<Device> devices = deviceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        devices.forEach(device -> {
            String newStatus = deviceService.calculateStatus(device.getLastSeenAt(), now);
            if (!newStatus.equals(device.getStatus())) {
                log.info("Device {} status changed from {} to {}", device.getDeviceId(), device.getStatus(), newStatus);
                device.setStatus(newStatus);
                deviceRepository.save(device);
            }
        });
    }
}
