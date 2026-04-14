package com.greenpulse.api.features.devices.service;

import com.greenpulse.api.features.devices.data.Device;
import com.greenpulse.api.features.devices.data.DeviceRepository;
import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.devices.dto.DeviceRegistrationRequest;
import com.greenpulse.api.features.devices.dto.DeviceResponse;
import com.greenpulse.api.features.devices.dto.DeviceUpdateRequest;
import com.greenpulse.api.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceResponse registerDevice(DeviceRegistrationRequest request, User owner) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElse(new Device());

        if (device.getOwner() != null && !device.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Device already registered by another user");
        }

        device.setDeviceId(request.getDeviceId());
        device.setName(request.getName());
        device.setType(request.getType());
        device.setLocation(request.getLocation());
        device.setCurrentCropId(request.getCropId());
        device.setStatus("ONLINE");
        device.setOwner(owner);

        deviceRepository.save(device);
        return mapToResponse(device);
    }

    public List<DeviceResponse> getAllDevices(User owner) {
        return deviceRepository.findByOwner(owner).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DeviceResponse getDeviceById(String deviceId, User owner) {
        Device device = deviceRepository.findByDeviceIdAndOwner(deviceId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        return mapToResponse(device);
    }

    @Transactional
    public void updateHeartbeat(String deviceId, User owner) {
        deviceRepository.findByDeviceIdAndOwner(deviceId, owner).ifPresent(device -> {
            device.setLastSeenAt(LocalDateTime.now());
            device.setStatus("ONLINE");
            deviceRepository.save(device);
        });
    }

    @Transactional
    public DeviceResponse updateDevice(String deviceId, DeviceUpdateRequest request, User owner) {
        Device device = deviceRepository.findByDeviceIdAndOwner(deviceId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        if (request.getName() != null)
            device.setName(request.getName());
        if (request.getLocation() != null)
            device.setLocation(request.getLocation());

        deviceRepository.save(device);
        return mapToResponse(device);
    }

    @Transactional
    public DeviceResponse assignCrop(String deviceId, String cropId, User owner) {
        Device device = deviceRepository.findByDeviceIdAndOwner(deviceId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        device.setCurrentCropId(cropId);
        deviceRepository.save(device);
        return mapToResponse(device);
    }

    public String calculateStatus(LocalDateTime lastSeen, LocalDateTime now) {
        if (lastSeen == null)
            return "OFFLINE";
        long minutes = ChronoUnit.MINUTES.between(lastSeen, now);
        if (minutes < 30)
            return "ONLINE";
        if (minutes < 120)
            return "DELAYED";
        return "OFFLINE";
    }

    private DeviceResponse mapToResponse(Device device) {
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .lastSeenAt(device.getLastSeenAt())
                .status(device.getStatus())
                .build();
    }
}
