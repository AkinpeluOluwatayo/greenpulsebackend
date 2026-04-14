package com.greenpulse.api.features.telemetry.service;

import com.greenpulse.api.features.devices.service.DeviceService;
import com.greenpulse.api.features.devices.data.Device;
import com.greenpulse.api.features.devices.data.DeviceRepository;
import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.telemetry.data.*;
import com.greenpulse.api.features.telemetry.dto.*;
import com.greenpulse.api.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final SensorReadingRepository readingRepository;
    private final CropProfileRepository profileRepository;
    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    @Transactional
    public void processTelemetry(String deviceId, Double temp, Double hum, Double soil) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        User owner = device.getOwner();

        deviceService.updateHeartbeat(deviceId, owner);
        CropProfile profile = profileRepository.findByIdAndOwner("tomatoes", owner)
                .orElseGet(() -> createDefaultProfile(owner));
        SensorReading.Status status = evaluateStatus(temp, hum, soil, profile);

        SensorReading reading = SensorReading.builder()
                .deviceId(deviceId)
                .temperature(temp)
                .humidity(hum)
                .soilMoisture(soil)
                .timestamp(LocalDateTime.now())
                .status(status)
                .owner(owner)
                .build();
        readingRepository.save(reading);

        if (status == SensorReading.Status.DANGER) {
            triggerAlert(deviceId, "Critical threshold breach detected!", owner);
        }
    }

    public List<TelemetryReadingDto> getHistory(String deviceId, User owner) {
        List<SensorReading> readings;
        if (deviceId != null) {
            readings = readingRepository.findByDeviceIdAndOwnerOrderByTimestampDesc(deviceId, owner);
        } else {
            readings = readingRepository.findByOwner(owner);
        }
        return readings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TelemetryReadingDto getLatestReading(String deviceId, User owner) {
        return readingRepository.findByDeviceIdAndOwnerOrderByTimestampDesc(deviceId, owner).stream()
                .findFirst()
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("SensorReading", deviceId));
    }

    public List<Alert> getActiveAlerts(String deviceId, User owner) {
        return alertRepository.findByDeviceIdAndOwnerAndResolvedFalse(deviceId, owner);
    }

    public List<AlertResponse> getAllAlerts(User owner) {
        return alertRepository.findByOwner(owner).stream()
                .map(a -> AlertResponse.builder()
                        .id(a.getId())
                        .deviceId(a.getDeviceId())
                        .message(a.getMessage())
                        .severity(a.getSeverity())
                        .timestamp(a.getTimestamp())
                        .resolved(a.isResolved())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAlertAsRead(Long alertId, User owner) {
        Alert alert = alertRepository.findByIdAndOwner(alertId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId.toString()));
        alert.setResolved(true);
        alertRepository.save(alert);
    }

    public SummaryResponse getSummary(User owner) {
        List<SensorReading> all = readingRepository.findByOwner(owner);
        long activeCount = deviceService.getAllDevices(owner).size();
        long openAlerts = alertRepository.findByOwner(owner).stream().filter(a -> !a.isResolved()).count();

        return SummaryResponse.builder()
                .averageTemperature(all.stream().mapToDouble(SensorReading::getTemperature).average().orElse(0.0))
                .averageHumidity(all.stream().mapToDouble(SensorReading::getHumidity).average().orElse(0.0))
                .averageSoilMoisture(all.stream().mapToDouble(SensorReading::getSoilMoisture).average().orElse(0.0))
                .activeDevicesCount(activeCount)
                .openAlertsCount(openAlerts)
                .build();
    }

    public List<CropResponse> getAllCrops(User owner) {
        return profileRepository.findByOwner(owner).stream()
                .map(this::mapToCropResponse)
                .collect(Collectors.toList());
    }

    public CropResponse getCrop(String id, User owner) {
        return profileRepository.findByIdAndOwner(id, owner)
                .map(this::mapToCropResponse)
                .orElseThrow(() -> new ResourceNotFoundException("CropProfile", id));
    }

    @Transactional
    public CropResponse createCrop(CropRequest request, User owner) {
        CropProfile profile = CropProfile.builder()
                .id(request.getId())
                .name(request.getName())
                .tempMin(request.getTempMin())
                .tempMax(request.getTempMax())
                .humidityMin(request.getHumidityMin())
                .humidityMax(request.getHumidityMax())
                .soilMoistureMin(request.getSoilMoistureMin())
                .soilMoistureMax(request.getSoilMoistureMax())
                .owner(owner)
                .build();
        profileRepository.save(profile);
        return mapToCropResponse(profile);
    }

    @Transactional
    public void updateThresholds(CropThresholdRequest request, User owner) {
        CropProfile profile = profileRepository.findByIdAndOwner(request.getProfileId(), owner)
                .orElseThrow(() -> new ResourceNotFoundException("CropProfile", request.getProfileId()));

        profile.setTempMin(request.getTempMin());
        profile.setTempMax(request.getTempMax());
        profile.setHumidityMin(request.getHumidityMin());
        profile.setHumidityMax(request.getHumidityMax());
        profile.setSoilMoistureMin(request.getSoilMoistureMin());
        profile.setSoilMoistureMax(request.getSoilMoistureMax());

        profileRepository.save(profile);
    }

    private SensorReading.Status evaluateStatus(Double temp, Double hum, Double soil, CropProfile p) {
        if (isOutOfRange(temp, p.getTempMin(), p.getTempMax()) ||
                isOutOfRange(hum, p.getHumidityMin(), p.getHumidityMax()) ||
                isOutOfRange(soil, p.getSoilMoistureMin(), p.getSoilMoistureMax())) {
            return SensorReading.Status.DANGER;
        }
        return SensorReading.Status.OPTIMAL;
    }

    private boolean isOutOfRange(Double val, Double min, Double max) {
        return val != null && (val < min || val > max);
    }

    private void triggerAlert(String deviceId, String msg, User owner) {
        Alert alert = Alert.builder()
                .deviceId(deviceId)
                .message(msg)
                .severity("DANGER")
                .timestamp(LocalDateTime.now())
                .resolved(false)
                .owner(owner)
                .build();
        alertRepository.save(alert);
    }

    private CropProfile createDefaultProfile(User owner) {
        CropProfile p = CropProfile.builder()
                .id("tomatoes").name("Tomatoes")
                .tempMin(10.0).tempMax(40.0).humidityMin(30.0).humidityMax(90.0).soilMoistureMin(20.0)
                .soilMoistureMax(80.0)
                .owner(owner)
                .build();
        return profileRepository.save(p);
    }

    private TelemetryReadingDto mapToDto(SensorReading r) {
        return TelemetryReadingDto.builder()
                .deviceId(r.getDeviceId())
                .temperature(r.getTemperature())
                .humidity(r.getHumidity())
                .soilMoisture(r.getSoilMoisture())
                .timestamp(r.getTimestamp())
                .status(r.getStatus().name())
                .build();
    }

    private CropResponse mapToCropResponse(CropProfile p) {
        return CropResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .tempMin(p.getTempMin())
                .tempMax(p.getTempMax())
                .humidityMin(p.getHumidityMin())
                .humidityMax(p.getHumidityMax())
                .soilMoistureMin(p.getSoilMoistureMin())
                .soilMoistureMax(p.getSoilMoistureMax())
                .build();
    }
}
