package dev.x341.aonbas2srv.services;

import com.google.inject.Inject;
import dev.x341.aonbas2srv.util.AOBLogger;

import java.io.IOException;

/**
 * MetroService provides a thin application-level facade over the TMB API client.
 * It catches IOExceptions from the API client, logs them, and returns a small JSON
 * error payload so callers (HTTP handlers) can return consistent error responses.
 *
 * This implementation also uses CacheService to reduce repeated API calls for the
 * same resources. On API failure the service will attempt to return a cached value
 * when available.
 */
public class MetroService {
    private final TmbApiClient tmbApiClient;
    private final CacheService cacheService;

    private static final String KEY_LINES = "lines";
    private static final String KEY_STATIONS_PREFIX = "stations:";        // stations:<lineCode>
    private static final String KEY_TRAINS_PREFIX = "trains:";            // trains:<stationCode>
    private static final String KEY_INTERCHANGES_PREFIX = "interchanges:"; // interchanges:<lineCode>:<stationCode>

    @Inject
    public MetroService(TmbApiClient tmbApiClient, CacheService cacheService) {

        this.tmbApiClient = tmbApiClient;
        this.cacheService = cacheService;
    }

    /**
     * Retrieve the list of metro lines as a JSON string.
     * Checks the cache first; on cache miss it calls the API and stores the result.
     * On API error the cached value is returned when available.
     *
     * @return JSON string with metro lines or an error JSON when the API call fails
     */
    public String getLines() {
        String cached = cacheService.get(KEY_LINES);
        if (cached != null) {
            AOBLogger.log("Returning cached metro lines");
            return cached;
        }

        try {
            String result = tmbApiClient.getMetroLines();
            if (result != null) {
                cacheService.put(KEY_LINES, result);
            }
            return result;
        } catch (IOException e) {
            AOBLogger.error("Error getting metro lines", e);
            // fallback to cached value if available
            if (cached != null) {
                AOBLogger.log("Falling back to cached metro lines after API error");
                return cached;
            }
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Retrieve stations for a given line code.
     * Uses the cache with key "stations:<lineCode>" and falls back to cached data on error.
     *
     * @param lineCode line identifier (e.g. "1" equals L1)
     * @return JSON string with stations or an error JSON when the API call fails
     */
    public String getStationForLine(String lineCode) {
        String key = KEY_STATIONS_PREFIX + lineCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            AOBLogger.log("Returning cached stations for line: " + lineCode);
            return cached;
        }

        try {
            String result = tmbApiClient.getStationOnLine(lineCode);
            if (result != null) {
                cacheService.put(key, result);
            }
            return result;
        } catch (IOException e) {
            AOBLogger.error("Error getting stations for line " + lineCode, e);
            if (cached != null) {
                AOBLogger.log("Falling back to cached stations for line " + lineCode);
                return cached;
            }
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Retrieve train arrival times for a station.
     * Uses the cache with key "trains:<stationCode>" and falls back to cached data on error.
     *
     * @param stationCode station identifier
     * @return JSON string with train times or an error JSON when the API call fails
     */
    public String getTrainTimes(String stationCode) {
        String key = KEY_TRAINS_PREFIX + stationCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            AOBLogger.log("Returning cached train times for station: " + stationCode);
            return cached;
        }

        try {
            String result = tmbApiClient.getTrainsForStation(stationCode);
            if (result != null) {
                cacheService.put(key, result);
            }
            return result;
        } catch (IOException e) {
            AOBLogger.error("Error getting trains for station " + stationCode, e);
            if (cached != null) {
                AOBLogger.log("Falling back to cached train times for station " + stationCode);
                return cached;
            }
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Retrieve interchanges for a given line and station.
     * Caches the response using key "interchanges:<lineCode>:<stationCode>".
     *
     * @param lineCode the line identifier
     * @param stationCode the station identifier
     * @return JSON string with interchanges or an error JSON when the API call fails
     */
    public String getInterchanges(String lineCode, String stationCode) {
        String key = KEY_INTERCHANGES_PREFIX + lineCode + ":" + stationCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            AOBLogger.log("Returning cached interchanges for " + lineCode + "/" + stationCode);
            return cached;
        }

        try {
            String result = tmbApiClient.getInterchanges(lineCode, stationCode);
            if (result != null) {
                cacheService.put(key, result);
            }
            return result;
        } catch (IOException e) {
            AOBLogger.error("Error getting interchanges for station " + stationCode, e);
            if (cached != null) {
                AOBLogger.log("Falling back to cached interchanges for " + lineCode + "/" + stationCode);
                return cached;
            }
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Helper to produce a small JSON error object. Centralizes the format used by the service.
     * The JSON produced has two fields: "error" and "message".
     *
     * @param errorCode short error code
     * @param e the exception that caused the error (message is used in the payload)
     * @return JSON string with error details
     */
    private String formatError(String errorCode, Exception e) {
        String msg = e == null ? "" : e.getMessage();
        // produce: {"error":"API_ERROR","message":"..."}
        return String.format("{\"error\":\"%s\",\"message\":\"%s\"}", errorCode, escapeJson(msg));
    }

    /**
     * Minimal string escaper for JSON string values. This escapes backslashes and double quotes
     * so the produced JSON remains valid for typical error messages.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
