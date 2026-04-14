package com.greenpulse.api.features.telemetry.controller;

import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.telemetry.data.Alert;
import com.greenpulse.api.features.telemetry.dto.*;
import com.greenpulse.api.features.telemetry.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/history")
    public ResponseEntity<List<TelemetryReadingDto>> getHistory(
            @RequestParam(required = false) String deviceId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getHistory(deviceId, user));
    }

    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<TelemetryReadingDto> getLatest(
            @PathVariable String deviceId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getLatestReading(deviceId, user));
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getSummary(user));
    }

    // Crops
    @GetMapping("/crops")
    public ResponseEntity<List<CropResponse>> getCrops(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getAllCrops(user));
    }

    @GetMapping("/crops/{id}")
    public ResponseEntity<CropResponse> getCrop(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getCrop(id, user));
    }

    @PostMapping("/crops")
    public ResponseEntity<CropResponse> createCrop(
            @RequestBody CropRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.createCrop(request, user));
    }

    // Alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<AlertResponse>> getAlerts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getAllAlerts(user));
    }

    @GetMapping("/alerts/{deviceId}")
    public ResponseEntity<List<Alert>> getActiveAlerts(
            @PathVariable String deviceId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(telemetryService.getActiveAlerts(deviceId, user));
    }

    @PatchMapping("/alerts/{id}/read")
    public ResponseEntity<Void> markAlertAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        telemetryService.markAlertAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/thresholds")
    public ResponseEntity<Void> updateThresholds(
            @RequestBody CropThresholdRequest request,
            @AuthenticationPrincipal User user) {
        telemetryService.updateThresholds(request, user);
        return ResponseEntity.ok().build();
    }
}
