package dev.x341.aonbas2srv.services.apiclients;

import com.google.gson.Gson;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.dto.MetroDto;
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
    private final Gson gson = new Gson();
    private static final String BASE_URL = "https://api.tmb.cat/v1";

    private static final String KEY_LINES = "tmb:lines";
    private static final String KEY_STATIONS_PREFIX = "tmb:stations:";
    private static final String KEY_INTERCHANGES_PREFIX = "tmb:interchanges:";
    private static final String KEY_TRAINS_PREFIX = "tmb:trains:";

    private static final int TRAIN_DATA_TTL_SECONDS = 10;

    @Inject
    public TmbApiClient(AOBConfig config, CacheService cacheService) {
        this.client = new OkHttpClient();
        this.authParams = String.format("app_id=%s&app_key=%s", config.getTmbAppId(), config.getTmbAppKey());
        this.cacheService = cacheService;
    }

    private String buildUrl(String endpoint) {
        return String.format("%s/%s?%s", BASE_URL, endpoint, authParams);
    }

    private String executeGetUrl(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        AOBLogger.log("Calling TMB API: " + url);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("TMB API call failed: " + response.code());
            return response.body().string();
        }
    }

    private String executeTmbCall(String endpoint) throws IOException {
        return executeGetUrl(buildUrl(endpoint));
    }

    // -------------------- LINES --------------------
    public String getMetroLinesJson() throws IOException {
        String cached = cacheService.get(KEY_LINES);
        if (cached != null) return cached;
        String result = executeTmbCall("transit/linies/metro");
        if (result != null) cacheService.put(KEY_LINES, result);
        return result;
    }

    public MetroDto getMetroLinesDto() throws IOException {
        return gson.fromJson(getMetroLinesJson(), MetroDto.class);
    }

    // -------------------- STATIONS --------------------
    public String getStationsForLineJson(String lineCode) throws IOException {
        String key = KEY_STATIONS_PREFIX + lineCode;
        String cached = cacheService.get(key);
        if (cached != null) return cached;
        String endpoint = String.format("transit/linies/metro/%s/estacions", lineCode);
        String result = executeTmbCall(endpoint);
        if (result != null) cacheService.put(key, result);
        return result;
    }

    public MetroDto getStationsForLineDto(String lineCode) throws IOException {
        return gson.fromJson(getStationsForLineJson(lineCode), MetroDto.class);
    }

    // -------------------- TRAINS --------------------
    public String getTrainsForStationJson(String stationCode) throws IOException {
        String key = KEY_TRAINS_PREFIX + stationCode;
        String cached = cacheService.get(key);
        if (cached != null) return cached;
        String url = String.format("%s/itransit/metro/estacions?estacions=%s&%s", BASE_URL, stationCode, authParams);
        String result = executeGetUrl(url);
        if (result != null) cacheService.put(key, result, TRAIN_DATA_TTL_SECONDS);
        return result;
    }

    public MetroDto getTrainsForStationDto(String stationCode) throws IOException {
        return gson.fromJson(getTrainsForStationJson(stationCode), MetroDto.class);
    }

    // -------------------- INTERCHANGES --------------------
    public String getInterchangesJson(String lineCode, String stationCode) throws IOException {
        String key = KEY_INTERCHANGES_PREFIX + lineCode + ":" + stationCode;
        String cached = cacheService.get(key);
        if (cached != null) return cached;
        String endpoint = String.format("transit/linies/metro/%s/estacions/%s/corresp", lineCode, stationCode);
        String result = executeTmbCall(endpoint);
        if (result != null) cacheService.put(key, result);
        return result;
    }

    public MetroDto getInterchangesDto(String lineCode, String stationCode) throws IOException {
        return gson.fromJson(getInterchangesJson(lineCode, stationCode), MetroDto.class);
    }
}
