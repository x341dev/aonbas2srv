package dev.x341.aonbas2srv.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.dto.MetroDto;
import dev.x341.aonbas2srv.dto.MetroDto.Feature;
import dev.x341.aonbas2srv.services.apiclients.TmbApiClient;
import dev.x341.aonbas2srv.util.AOBLogger;

import java.io.IOException;

public class MetroService {
    private final TmbApiClient tmbApiClient;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    private static final String KEY_LINES = "lines";
    private static final String KEY_STATIONS_PREFIX = "stations:";

    @Inject
    public MetroService(TmbApiClient tmbApiClient, CacheService cacheService, ObjectMapper objectMapper) {
        this.tmbApiClient = tmbApiClient;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    // -------------------- LINES --------------------
    public MetroDto getLinesDto() {
        String cached = cacheService.get(KEY_LINES);
        if (cached != null) {
            try { return objectMapper.readValue(cached, MetroDto.class); }
            catch (IOException e) { AOBLogger.error("Error parsing cached lines JSON", e); }
        }
        try {
            MetroDto dto = tmbApiClient.getMetroLinesDto();
            if (dto != null) cacheService.put(KEY_LINES, objectMapper.writeValueAsString(dto));
            return dto;
        } catch (IOException e) {
            AOBLogger.error("Error getting metro lines", e);
            if (cached != null) {
                try { return objectMapper.readValue(cached, MetroDto.class); }
                catch (IOException ex) { AOBLogger.error("Error parsing fallback cached lines JSON", ex); }
            }
            return null;
        }
    }

    public String getLinesJson() {
        MetroDto dto = getLinesDto();
        try { return objectMapper.writeValueAsString(dto); }
        catch (IOException e) { AOBLogger.error("Error converting lines DTO to JSON", e); return "{}"; }
    }

    // -------------------- STATIONS --------------------
    public MetroDto getStationForLineDto(String lineCode) {
        String key = KEY_STATIONS_PREFIX + lineCode;
        String cached = cacheService.get(key);
        if (cached != null) {
            try { return objectMapper.readValue(cached, MetroDto.class); }
            catch (IOException e) { AOBLogger.error("Error parsing cached stations JSON", e); }
        }
        try {
            MetroDto dto = tmbApiClient.getStationsForLineDto(lineCode);
            if (dto != null) cacheService.put(key, objectMapper.writeValueAsString(dto));
            return dto;
        } catch (IOException e) {
            AOBLogger.error("Error getting stations for line " + lineCode, e);
            if (cached != null) {
                try { return objectMapper.readValue(cached, MetroDto.class); }
                catch (IOException ex) { AOBLogger.error("Error parsing fallback cached stations JSON", ex); }
            }
            return null;
        }
    }

    public String getStationForLine(String lineCode) {
        MetroDto dto = getStationForLineDto(lineCode);
        try { return objectMapper.writeValueAsString(dto); }
        catch (IOException e) { AOBLogger.error("Error converting stations DTO to JSON", e); return "{}"; }
    }

    // -------------------- TRAINS --------------------
    public MetroDto getTrainTimesDto(String stationCode) {
        try { return tmbApiClient.getTrainsForStationDto(stationCode); }
        catch (IOException e) { AOBLogger.error("Error getting train times for " + stationCode, e); return null; }
    }

    public String getTrainTimes(String stationCode) {
        MetroDto dto = getTrainTimesDto(stationCode);
        try { return objectMapper.writeValueAsString(dto); }
        catch (IOException e) { AOBLogger.error("Error converting train times DTO to JSON", e); return "{}"; }
    }

    // -------------------- INTERCHANGES --------------------
    public MetroDto getInterchangesDto(String lineCode, String stationCode) {
        try { return tmbApiClient.getInterchangesDto(lineCode, stationCode); }
        catch (IOException e) { AOBLogger.error("Error getting interchanges for " + lineCode + "/" + stationCode, e); return null; }
    }

    public String getInterchanges(String lineCode, String stationCode) {
        MetroDto dto = getInterchangesDto(lineCode, stationCode);
        try { return objectMapper.writeValueAsString(dto); }
        catch (IOException e) { AOBLogger.error("Error converting interchanges DTO to JSON", e); return "{}"; }
    }
}
