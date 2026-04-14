package com.greenpulse.api.features.telemetry.data;

import com.greenpulse.api.features.auth.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CropProfileRepository extends JpaRepository<CropProfile, String> {
    List<CropProfile> findByOwner(User owner);

    Optional<CropProfile> findByIdAndOwner(String id, User owner);
}
