package dev.x341.aonbas2srv.services;

import com.google.transit.realtime.GtfsRealtime;
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

    private final Map<String, ApiCacheEntry> apiCache;

    private final Map<String, GtfsCacheEntry> gtfsCache;

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
            protected boolean removeEldestEntry(Map.Entry<String, ApiCacheEntry> eldest) {
                boolean remove = size() > MAX_API_CALLS;
                if (remove) {
                    AOBLogger.log("Removed old entry from cache: " + eldest.getKey());
                }
                return remove;
            }
        };
        this.gtfsCache = new LinkedHashMap<>();
        AOBLogger.log("Cache initialized with " + MAX_API_CALLS + " capacity.");
    }

    /**
     * Retrieve a value from the cache by key.
     *
     * @param key the cache key
     * @return the cached value or null if not present
     */
    public String get(String key) {
        ApiCacheEntry entry = apiCache.get(key);
        if (entry != null) {
            if (!entry.expired()) {
                AOBLogger.log("Cache hit for: " + key);
                return entry.value;
            } else {
                apiCache.remove(key);
                AOBLogger.log("Cache expired for: " + key);
            }
        }
        return null;
    }

    /**
     * Put a key/value pair into the cache with time to live. If the value is null the key will be removed.
     * If the cache size exceeds capacity the oldest entry will be evicted automatically.
     *
     * @param key the cache key
     * @param value the value to store, or null to remove the key
     * @param ttlSeconds the time to live in seconds
     */
    public void put(String key, String value, int ttlSeconds) {
        if (value == null) {
            apiCache.remove(key);
            AOBLogger.log("Cache REMOVE for key: " + key);
            return;
        }
        apiCache.put(key, new ApiCacheEntry(value, ttlSeconds));
        AOBLogger.log("Cache PUT for key: " + key + " with TTL " + ttlSeconds + "s");
    }

    /**
     * Put a key/value pair into the cache. If the value is null the key will be removed.
     * If the cache size exceeds capacity the oldest entry will be evicted automatically.
     *
     * @param key   the cache key
     * @param value the value to store, or null to remove the key
     */
    public void put(String key, String value) {
        put(key, value, 31536000);
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
        // Usa la versión que llama a put(key, value, 31536000) si usas la sobrecarga
        put(TRAM_TOKEN_KEY, token);
        AOBLogger.log("Tram access token stored in cache");
    }

    /**
     * Retrieve the tram access token from the cache.
     *
     * @return the token if present, otherwise null
     */
    public String getTramAccessToken() {
        ApiCacheEntry entry = apiCache.get(TRAM_TOKEN_KEY);
        return get(TRAM_TOKEN_KEY);
    }
    /**
     * Clear all entries from the cache.
     */
    public void clear() {
        apiCache.clear();
        AOBLogger.log("Cache cleared manually");
    }


    private static class GtfsCacheEntry {
        final GtfsRealtime.FeedMessage feed;
        final long expiresAt;

        GtfsCacheEntry(GtfsRealtime.FeedMessage feed, int ttlSecconds) {
            this.feed = feed;
            this.expiresAt = System.currentTimeMillis()/1000 + ttlSecconds;
        }

        boolean expired() {
            return System.currentTimeMillis()/1000 >= expiresAt;
        }
    }

    /**
     * Get a GTFS-RT feed from cache if present and not expired.
     * @param cacheKey network or identifier
     * @return feed or null if not cached/expired
     */
    public GtfsRealtime.FeedMessage getGtfsRt(String cacheKey) {
        GtfsCacheEntry entry = gtfsCache.get(cacheKey);
        if (entry != null) {
            if (!entry.expired()) {
                AOBLogger.log("GTFS-RT cache hit for " + cacheKey);
                return entry.feed;
            } else {
                gtfsCache.remove(cacheKey);
                AOBLogger.log("GTFS-RT cache expired for " + cacheKey);
            }
        }
        return null;
    }

    /**
     * Put a GTFS-RT feed into cache with TTL.
     * @param cacheKey network or identifier
     * @param feed feed to store
     * @param ttlSeconds seconds until expiration
     */
    public void putGtfsRt(String cacheKey, GtfsRealtime.FeedMessage feed, int ttlSeconds) {
        gtfsCache.put(cacheKey, new GtfsCacheEntry(feed, ttlSeconds));
        AOBLogger.log("GTFS-RT cached for " + cacheKey + "with TTL " + ttlSeconds + "s");
    }


    public static class ApiCacheEntry {
        final String value;
        final long ttlSeconds;

        ApiCacheEntry(String value, int ttlSeconds) {
            this.value = value;
            this.ttlSeconds = System.currentTimeMillis() + (long)ttlSeconds * 1000L;
        }

        boolean expired() {
            return System.currentTimeMillis() >= ttlSeconds;
        }
    }
}
