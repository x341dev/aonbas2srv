package dev.x341.aonbas2srv.services.apiclients;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.transit.realtime.GtfsRealtime.*;
import dev.x341.aonbas2srv.services.CacheService;
import dev.x341.aonbas2srv.util.AOBLogger;
import okhttp3.*;

import java.io.IOException;

public class TramApiClient {
    private static final String BASE_URL = "https://opendata.tram.cat/api/v1";
    private final OkHttpClient client = new OkHttpClient();
    private final CacheService cacheService;

    @Inject
    public TramApiClient(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public FeedMessage getGtfsrtData(String network) throws IOException {
        String cacheKey = "gtfs:rt:" + network;
        FeedMessage cached = cacheService.getGtfsRt(cacheKey);

        if (cached != null) return cached;

        String networkId = network.equalsIgnoreCase("TRAMBESOS") ? "1" : "2";

        Request tripReq = new Request.Builder()
                .url(BASE_URL + "/gtfsrealtime?networkId=" + networkId)
                .build();

        Request vehicleReq = new Request.Builder()
                .url(BASE_URL + "/gtfsrealtime/vehicleUpdate?networkId=" + networkId)
                .build();

        FeedMessage.Builder meged = FeedMessage.newBuilder();
        try (Response tripRes = client.newCall(tripReq).execute(); Response vehRes = client.newCall(vehicleReq).execute();) {
            FeedMessage tripFeed = FeedMessage.parseFrom(tripRes.body().bytes());
            FeedMessage vehFeed = FeedMessage.parseFrom(vehRes.body().bytes());

            meged.addAllEntity(tripFeed.getEntityList());
            meged.addAllEntity(vehFeed.getEntityList());
        }

        FeedMessage feed = meged.build();
        cacheService.putGtfsRt(cacheKey, feed, 30);
        return feed;
    }

    public JsonArray getLines() throws IOException {
        Request req = new Request.Builder()
                .url(BASE_URL + "/lines?page=0&pageSize=100")
                .build();

        try (Response res = client.newCall(req).execute()) {
            return JsonParser.parseString(res.body().string()).getAsJsonArray();
        }
    }

    public JsonArray getStops(String lineId) throws IOException {
        Request req = new Request.Builder()
                .url(BASE_URL + "/lines/" + lineId + "/stops?page=0&pageSize=100")
                .build();

        try (Response res = client.newCall(req).execute()) {
            return JsonParser.parseString(res.body().string()).getAsJsonArray();
        }
    }

    public void getTime(String stopGtfsCode, String network) throws IOException {
        FeedMessage feed = getGtfsrtData(network);
        long now = System.currentTimeMillis() / 1000;

        for (FeedEntity entity : feed.getEntityList()) {
            if (!entity.hasTripUpdate()) continue;
            TripUpdate trip = entity.getTripUpdate();

            for (TripUpdate.StopTimeUpdate stopUpdate : trip.getStopTimeUpdateList()) {
                if (stopUpdate.getStopId().equalsIgnoreCase(stopGtfsCode)) {
                    long arrivalSec = stopUpdate.getArrival().getTime();
                    long diffMin = (arrivalSec - now) / 60;
                    AOBLogger.debug("Trip " + trip.getTrip().getTripId() +
                            " | Line " + trip.getTrip().getRouteId() +
                            " → " + diffMin + " min to " + stopGtfsCode);
                }
            }
        }
    }
}
