package dev.x341.aonbas2srv.services;

import dev.x341.aonbas2srv.util.AOBLogger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple small in-memory cache used to store recent API responses and tokens.
 * <p>
 * The cache keeps up to MAX_API_CALLS entries and evicts the eldest entry when capacity is exceeded.
 */
public class CacheService {

    private static final int MAX_API_CALLS = 10;
    private static final String TRAM_TOKEN_KEY = "tram_access_token";

    private final Map<String, String> apiCache;

    /**
     * Create a CacheService instance with a bounded LinkedHashMap that evicts oldest entries when
     * size exceeds MAX_API_CALLS.
     */
    public CacheService() {
        this.apiCache = new LinkedHashMap<>(MAX_API_CALLS, 0.75f, false) {

            /**
             * Returns true if the oldest entry must be removed
             *
             * @param eldest the eldest entry in the map
             * @return true if the map size exceeded the configured capacity
             */
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                boolean remove = size() > MAX_API_CALLS;
                if (remove) {
                    AOBLogger.log("Removed old entry from cache: " + eldest.getKey());
                }
                return remove;
            }
        };
        AOBLogger.log("Cache initialized with " + MAX_API_CALLS + " capacity.");
    }

    /**
     * Retrieve a value from the cache by key.
     *
     * @param key the cache key
     * @return the cached value or null if not present
     */
    public String get(String key) {
        String data = apiCache.get(key);
        if (data != null) {
            AOBLogger.log("Cache hit for: " + key);
        }
        return data;
    }

    /**
     * Put a key/value pair into the cache. If the value is null the key will be removed.
     * If the cache size exceeds capacity the oldest entry will be evicted automatically.
     *
     * @param key   the cache key
     * @param value the value to store, or null to remove the key
     */
    public void put(String key, String value) {
        if (value == null) {
            apiCache.remove(key);
            AOBLogger.log("Cache REMOVE for key: " + key);
            return;
        }
        apiCache.put(key, value);
        AOBLogger.log("Cache PUT for key: " + key);
    }

    /**
     * Store the tram access token in the cache using an internal key.
     *
     * This helper centralizes where the token is stored so callers don't need to know the cache key.
     *
     * @param token the tram access token to store; if null the token entry will be removed
     */
    public void putTramAccessToken(String token) {
        if (token == null) {
            apiCache.remove(TRAM_TOKEN_KEY);
            AOBLogger.log("Tram access token removed from cache");
            return;
        }
        apiCache.put(TRAM_TOKEN_KEY, token);
        AOBLogger.log("Tram access token stored in cache");
    }

    /**
     * Retrieve the tram access token from the cache.
     *
     * @return the token if present, otherwise null
     */
    public String getTramAccessToken() {
        return apiCache.get(TRAM_TOKEN_KEY);
    }

    /**
     * Clear all entries from the cache.
     */
    public void clear() {
        apiCache.clear();
        AOBLogger.log("Cache cleared manually");
    }


}
