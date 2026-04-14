package com.greenpulse.api.features.auth.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final long BLOCK_DURATION = TimeUnit.MINUTES.toMillis(15);
    private final ConcurrentHashMap<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blockCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        blockCache.remove(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.getOrDefault(key, 0);
        attempts++;
        attemptsCache.put(key, attempts);
        if (attempts >= MAX_ATTEMPT) {
            blockCache.put(key, System.currentTimeMillis() + BLOCK_DURATION);
        }
    }

    public boolean isBlocked(String key) {
        if (!blockCache.containsKey(key)) {
            return false;
        }
        long unblockTime = blockCache.get(key);
        if (System.currentTimeMillis() > unblockTime) {
            blockCache.remove(key);
            attemptsCache.remove(key);
            return false;
        }
        return true;
    }
}
