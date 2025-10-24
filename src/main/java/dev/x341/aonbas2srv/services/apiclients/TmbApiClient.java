package dev.x341.aonbas2srv.services.apiclients;

import com.google.inject.Inject;
import dev.x341.aonbas2srv.services.CacheService;
import dev.x341.aonbas2srv.util.AOBConfig;
import dev.x341.aonbas2srv.util.AOBLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class TmbApiClient {
    private final OkHttpClient client;
    private final String authParams;
    private final CacheService cacheService;
    private static final String BASE_URL = "https://api.tmb.cat/v1";

    private static final String KEY_LINES = "tmb:lines";
    private static final String KEY_STATIONS_PREFIX = "tmb:stations:"; // + lineCode
    private static final String KEY_INTERCHANGES_PREFIX = "tmb:interchanges:"; // + lineCode + ":" + stationCode
    private static final String KEY_TRAINS_PREFIX = "tmb:trains:"; // + stationCode

    @Inject
    public TmbApiClient(AOBConfig config, CacheService cacheService) {
        this.client = new OkHttpClient();
        this.authParams = String.format("app_id=%s&app_key=%s", config.getTmbAppId(), config.getTmbAppKey());
        this.cacheService = cacheService;
    }

    /**
     * Build a full URL for an endpoint under the TMB base API, automatically appending auth params.
     * Example: buildUrl("transit/linies/metro") -> https://api.tmb.cat/v1/transit/linies/metro?app_id=...&app_key=...
     *
     * @param endpoint endpoint path relative to the base API (no leading slash)
     * @return fully-qualified URL with authentication query parameters
     */
    private String buildUrl(String endpoint) {
        return String.format("%s/%s?%s", BASE_URL, endpoint, authParams);
    }

    /**
     * Execute a GET request against the provided fully-qualified URL and return the response body as string.
     * Commonizes the request/response handling so callers don't duplicate okhttp boilerplate.
     *
     * @param url fully-qualified URL to call
     * @return response body as string
     * @throws IOException if the HTTP call fails or returns a non-success status
     */
    private String executeGetUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        AOBLogger.log("Calling TMB API: " + url);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("TMB API call failed: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    /**
     * Execute a TMB API call for an endpoint relative to the base URL.
     * This will automatically build the URL (including auth params) and perform the GET.
     *
     * Note: responses are cached by this client using an internal key. Cache TTL is not enforced in
     * CacheService; callers that require fresh data should bypass or invalidate cache explicitly.
     *
     * @param endpoint endpoint path relative to the base API (no leading slash)
     * @return response body as string
     * @throws IOException if the HTTP call fails
     */
    private String executeTmbCall(String endpoint) throws IOException {
        String url = buildUrl(endpoint);
        return executeGetUrl(url);
    }

    /**
     * Get the list of metro lines from the TMB API.
     * This method first checks the cache and returns the cached value when available.
     * On cache miss it performs an API call and stores the result in cache.
     *
     * @return JSON string representing metro lines
     * @throws IOException when the API call fails
     */
    public String getMetroLines() throws IOException {
        String cached = cacheService.get(KEY_LINES);
        if (cached != null) {
            AOBLogger.log("TMB cache hit: lines");
            return cached;
        }

        String result = executeTmbCall("transit/linies/metro");
        if (result != null) {
            cacheService.put(KEY_LINES, result);
        }
        return result;
    }

    /**
     * Get stations for the specified metro line.
     * Uses the cache key "tmb:stations:{lineCode}".
     *
     * @param lineCode the line code (e.g. L1, L2)
     * @return JSON string with stations for the line
     * @throws IOException when the API call fails
     */
    public String getStationOnLine(String lineCode) throws IOException {
        String key = KEY_STATIONS_PREFIX + lineCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            AOBLogger.log("TMB cache hit: stations for " + lineCode);
            return cached;
        }

        String endpoint = String.format("transit/linies/metro/%s/estacions", lineCode);
        String result = executeTmbCall(endpoint);
        if (result != null) {
            cacheService.put(key, result);
        }
        return result;
    }

    /**
     * Get interchanges (correspondences) for a given line and station.
     * Uses the cache key "tmb:interchanges:{lineCode}:{stationCode}".
     *
     * @param lineCode the metro line code
     * @param stationCode the station code
     * @return JSON string with interchange information
     * @throws IOException when the API call fails
     */
    public String getInterchanges(String lineCode, String stationCode) throws IOException {
        String key = KEY_INTERCHANGES_PREFIX + lineCode + ":" + stationCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            AOBLogger.log("TMB cache hit: interchanges for " + lineCode + "/" + stationCode);
            return cached;
        }

        String endpoint = String.format("transit/linies/metro/%s/estacions/%s/corresp", lineCode, stationCode);
        String result = executeTmbCall(endpoint);
        if (result != null) {
            cacheService.put(key, result);
        }
        return result;
    }

    /**
     * Get train arrival times for a specific station. This endpoint uses a different path/query format,
     * so we build the full URL and use the common executeGetUrl helper. Response is cached under key
     * "tmb:trains:{stationCode}".
     *
     * @param stationCode station identifier to query
     * @return JSON string with trains information
     * @throws IOException when the API call fails
     */
    public String getTrainsForStation(String stationCode) throws IOException {
        String key = KEY_TRAINS_PREFIX + stationCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            AOBLogger.log("TMB cache hit: trains for " + stationCode);
            return cached;
        }

        String url = String.format("%s/itransit/metro/estacions?estacions=%s&%s", BASE_URL, stationCode, authParams);
        String result = executeGetUrl(url);
        if (result != null) {
            cacheService.put(key, result);
        }
        return result;
    }

}
