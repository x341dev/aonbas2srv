package dev.x341.aonbas2srv.services;

import com.google.inject.Inject;
import dev.x341.aonbas2srv.dto.OtpDto;
import dev.x341.aonbas2srv.util.AOBLogger;

import java.util.UUID;

/**
 * OtpService is a small helper that creates and stores OtpDto objects in the CacheService.
 *
 * It provides two main operations:
 * - createOtp(type, payload): creates an OtpDto with a generated id and stores it in cache
 * - getOtp(id): retrieves the serialized OtpDto from cache
 *
 * This keeps OTP transport simple (JSON) and language-agnostic for consumption by
 * Kotlin Multiplatform or other clients.
 */
public class OtpService {

    private final CacheService cacheService;
    private static final String OTP_KEY_PREFIX = "otp:";

    @Inject
    public OtpService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Create a new OtpDto, store it in cache and return the created object.
     * The object is stored under key "otp:{id}" so it can be retrieved later.
     *
     * @param type a short type string describing the payload (e.g. "station")
     * @param payload the string payload; keep it small (JSON or encoded data)
     * @return the created OtpDto
     */
    public OtpDto createOtp(String type, String payload) {
        String id = UUID.randomUUID().toString();
        long ts = System.currentTimeMillis();
        OtpDto dto = new OtpDto(id, type, payload, ts);
        String key = OTP_KEY_PREFIX + id;
        cacheService.put(key, dto.toJson());
        AOBLogger.log("OTP created and cached: " + key);
        return dto;
    }

    /**
     * Retrieve an OTP by id. Returns null if not found.
     *
     * @param id the OTP identifier
     * @return OtpDto instance or null when missing
     */
    public OtpDto getOtp(String id) {
        String key = OTP_KEY_PREFIX + id;
        String json = cacheService.get(key);
        if (json == null) {
            AOBLogger.log("OTP not found in cache: " + key);
            return null;
        }
        try {
            return OtpDto.fromJson(json);
        } catch (Exception e) {
            AOBLogger.error("Invalid OTP JSON stored for key: " + key, e);
            return null;
        }
    }

    /**
     * Remove an OTP from cache. Returns true if removed or false if not present.
     *
     * @param id the OTP identifier
     * @return true if removed, false otherwise
     */
    public boolean removeOtp(String id) {
        String key = OTP_KEY_PREFIX + id;
        String existing = cacheService.get(key);
        if (existing == null) return false;
        cacheService.put(key, null);
        AOBLogger.log("OTP removed from cache: " + key);
        return true;
    }
}

