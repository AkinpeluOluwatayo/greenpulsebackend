package com.greenpulse.api.features.security;

import com.greenpulse.api.features.auth.data.User;
import com.greenpulse.api.features.devices.data.Device;
import com.greenpulse.api.features.devices.data.DeviceRepository;
import com.greenpulse.api.features.devices.service.DeviceService;
import com.greenpulse.api.features.telemetry.data.Alert;
import com.greenpulse.api.features.telemetry.data.AlertRepository;
import com.greenpulse.api.features.telemetry.data.CropProfileRepository;
import com.greenpulse.api.features.telemetry.data.SensorReadingRepository;
import com.greenpulse.api.features.telemetry.service.TelemetryService;
import com.greenpulse.api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IDORSecurityTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private CropProfileRepository profileRepository;

    @InjectMocks
    private DeviceService deviceService;

    @InjectMocks
    private TelemetryService telemetryService;

    private User user1;
    private User user2;
    private Device user1Device;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("user1@example.com").build();
        user2 = User.builder().id(2L).email("user2@example.com").build();
        user1Device = Device.builder().deviceId("DEV-001").owner(user1).build();
    }

    @Test
    void user2ShouldNotAccessUser1Device() {
        // When device exists but owned by user1
        when(deviceRepository.findByDeviceIdAndOwner("DEV-001", user2)).thenReturn(Optional.empty());

        // Then getDeviceById should throw ResourceNotFoundException for user2
        assertThrows(ResourceNotFoundException.class, () -> {
            deviceService.getDeviceById("DEV-001", user2);
        });
    }

    @Test
    void user2ShouldNotAccessUser1Alerts() {
        // When alert exists but owned by user1
        when(alertRepository.findByIdAndOwner(100L, user2)).thenReturn(Optional.empty());

        // Then markAlertAsRead should throw ResourceNotFoundException for user2
        assertThrows(ResourceNotFoundException.class, () -> {
            telemetryService.markAlertAsRead(100L, user2);
        });
    }

    @Test
    void user2ShouldNotAccessUser1Crops() {
        // When crop exists but owned by user1
        when(profileRepository.findByIdAndOwner("tomatoes", user2)).thenReturn(Optional.empty());

        // Then getCrop should throw ResourceNotFoundException for user2
        assertThrows(ResourceNotFoundException.class, () -> {
            telemetryService.getCrop("tomatoes", user2);
        });
    }
}
