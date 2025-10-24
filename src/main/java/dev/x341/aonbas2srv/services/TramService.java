package dev.x341.aonbas2srv.services;

import com.google.transit.realtime.GtfsRealtime.*;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.dto.Stop;
import dev.x341.aonbas2srv.services.apiclients.TramApiClient;
import dev.x341.aonbas2srv.util.AOBLogger;

import java.io.IOException;
import java.util.*;

public class TramService {

    private final TramApiClient apiClient;
    private final Map<String, Stop> gtfsCodeMap = new HashMap<>(); // gtfsCode -> Stop



    @Inject
    public TramService(TramApiClient apiClient) {
        this.apiClient = apiClient;
        try {
            loadAllStops();
        } catch (IOException e) {
            AOBLogger.error("Failed to load stops during TramService init", e);
        }
    }

    /** Load all stops from API into map */
    private void loadAllStops() throws IOException {
        List<Stop> allStops = apiClient.getAllStops(); // llama a GET /api/v1/stops
        for (Stop s : allStops) {
            if (s == null) continue;
            String code = s.getGtfsCode();
            if (code == null) continue;
            gtfsCodeMap.put(code.toUpperCase(), s);
        }
        AOBLogger.log("Loaded " + allStops.size() + " stops from API");
    }

    /** Devuelve stops de una línea */
    public List<Stop> getStopsForLine(String lineId) throws IOException {
        List<Stop> stops = apiClient.getStops(lineId); // GET /api/v1/lines/{lineId}/stops
        for (Stop s : stops) {
            if (s == null) continue;
            String code = s.getGtfsCode();
            if (code == null) continue;
            gtfsCodeMap.put(code.toUpperCase(), s); // actualizar mapa
        }
        return stops;
    }

    /** Return all known GTFS codes (delegates to API client) */
    public Set<String> listAllGtfsCodes() throws IOException {
        return apiClient.listAllGtfsCodes();
    }

    /** Find GTFS-RT stop ids present in realtime feed but missing from static stops */
    public Set<String> findMissingStaticStopsInGtfsRt(String network) throws IOException {
        return apiClient.findMissingStaticStopsInGtfsRt(network);
    }

    /** Devuelve parada por gtfsCode */
    public Stop getStopByGtfsCode(String gtfsCode) {
        if (gtfsCode == null) return null;
        Stop s = gtfsCodeMap.get(gtfsCode.toUpperCase());
        return s;
    }

    /** Devuelve próximos trams para una parada usando GTFS */
    public String getStopTimes(String gtfsCode) throws IOException {
        Stop stop = getStopByGtfsCode(gtfsCode);
        if (stop == null) {
            // fallback: ask API client directly (handles flexible matching)
            stop = apiClient.getStop(gtfsCode);
            if (stop != null) {
                gtfsCodeMap.put(stop.getGtfsCode().toUpperCase(), stop);
            }
        }
        if (stop == null) {
            AOBLogger.log("Stop not found for gtfsCode: " + gtfsCode);
            return "";
        }
        // Check both networks' GTFS-RT feeds (TRAMBESOS and TRAMBAIX) so we find updates regardless of network
        String[] networks = new String[]{"TRAMBESOS", "TRAMBAIX"};
        long now = System.currentTimeMillis() / 1000;
        for (String net : networks) {
            FeedMessage feed = apiClient.getGtfsrtData(net);
            for (FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;
                TripUpdate trip = entity.getTripUpdate();

                for (TripUpdate.StopTimeUpdate stopUpdate : trip.getStopTimeUpdateList()) {
                    String stopId = stopUpdate.getStopId();
                    if (!stopId.equalsIgnoreCase(stop.getGtfsCode()) &&
                            !stopId.equalsIgnoreCase(String.valueOf(stop.getOutboundCode())) &&
                            !stopId.equalsIgnoreCase(String.valueOf(stop.getReturnCode())))
                        continue;

                    long arrivalSec;
                    try {
                        arrivalSec = stopUpdate.getArrival().getTime();
                    } catch (Exception e) {
                        continue;
                    }
                    if (arrivalSec <= 0) continue;

                    long diffMin = (arrivalSec - now) / 60;
                    AOBLogger.log("[" + net + "] Trip " + trip.getTrip().getTripId() +
                            " | Line " + trip.getTrip().getRouteId() +
                            " → " + diffMin + " min to stop " + stop.getName() +
                            " (" + stop.getGtfsCode() + ")");
                }
            }
        }
        return "Check console";
    }

    /** Devuelve info JSON de todas las líneas */
    public String getLinesJson() throws IOException {
        return apiClient.getLines();
    }

    /** Devuelve info JSON de una parada específica */
    public String getStopJson(String stopId) throws IOException {
        Stop stop = apiClient.getStop(stopId);
        return stop != null ? stop.toJson() : "{}";
    }

    /** Clear cached stops/lines and reload stops from API (useful during debugging). */
    public String refreshStops() {
        try {
            apiClient.clearAllApiCache();
            loadAllStops();
            return "ok";
        } catch (IOException e) {
            AOBLogger.error("Failed to refresh stops", e);
            return "error";
        }
    }
}
