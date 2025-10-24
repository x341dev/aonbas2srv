package dev.x341.aonbas2srv.services;


import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.transit.realtime.GtfsRealtime.*;
import dev.x341.aonbas2srv.services.apiclients.TramApiClient;
import dev.x341.aonbas2srv.util.AOBLogger;

import java.io.IOException;

public class TramService {
    private final TramApiClient apiClient;

    @Inject
    public TramService(CacheService cacheService) {
        this.apiClient = new TramApiClient(cacheService);
    }

    public void fetchAndLog(String network) throws IOException {
        AOBLogger.log("Downloading GTFS-RT feed for " + network);
        FeedMessage feed = apiClient.getGtfsrtData(network);

        feed.getEntityList().forEach(entity -> {
            if (entity.hasTripUpdate()) {
                var trip = entity.getTripUpdate();
                AOBLogger.debug("Trip " + trip.getTrip().getTripId() + " | Line " + trip.getTrip().getRouteId());
            }
        });
    }

    public String getLines() throws IOException {
        JsonArray lines = apiClient.getLines();
        return lines.getAsJsonArray().toString();
    }

    public String getStopsForLines(String lineId) throws IOException {
        JsonArray stops = apiClient.getStops(lineId);
        return stops.getAsJsonArray().toString();
    }
}
