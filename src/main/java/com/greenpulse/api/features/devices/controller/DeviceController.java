package com.greenpulse.api.features.devices.controller;

import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.devices.dto.DeviceRegistrationRequest;
import com.greenpulse.api.features.devices.dto.DeviceResponse;
import com.greenpulse.api.features.devices.dto.DeviceUpdateRequest;
import com.greenpulse.api.features.devices.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(
            @RequestBody DeviceRegistrationRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.registerDevice(request, user));
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> listDevices(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.getAllDevices(user));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> getDevice(
            @PathVariable String deviceId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.getDeviceById(deviceId, user));
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable String deviceId,
            @RequestBody DeviceUpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.updateDevice(deviceId, request, user));
    }

    @PatchMapping("/{deviceId}/assign-crop/{cropId}")
    public ResponseEntity<DeviceResponse> assignCrop(
            @PathVariable String deviceId,
            @PathVariable String cropId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.assignCrop(deviceId, cropId, user));
    }

    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<Void> updateHeartbeat(
            @PathVariable String deviceId,
            @AuthenticationPrincipal User user) {
        deviceService.updateHeartbeat(deviceId, user);
        return ResponseEntity.ok().build();
    }
}
