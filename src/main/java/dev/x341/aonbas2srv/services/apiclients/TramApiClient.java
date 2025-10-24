package dev.x341.aonbas2srv.services.apiclients;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.dto.Stop;
import dev.x341.aonbas2srv.services.CacheService;
import dev.x341.aonbas2srv.util.AOBLogger;
import com.google.transit.realtime.GtfsRealtime.*;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TramApiClient {
    private static final String BASE_URL = "https://opendata.tram.cat/api/v1";
    private final OkHttpClient client = new OkHttpClient();
    private final CacheService cacheService;
    private final Gson gson = new Gson();
    private static final int HTTP_MAX_RETRIES = 3;
    private static final long HTTP_BACKOFF_MS = 500;
    private static final int DEFAULT_PAGE_SIZE = 100;

    @Inject
    public TramApiClient(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /** Obtiene todas las líneas (cached as JSON string) */
    public String getLines() throws IOException {
        String cacheKey = "lines:all";
        String cached = cacheService.get(cacheKey);
        if (cached != null) return cached;

        Request req = new Request.Builder()
                .url(BASE_URL + "/lines?page=0&pageSize=100")
                .build();
        try (Response res = executeRequest(req)) {
            if (!res.isSuccessful() || res.body() == null)
                throw new IOException("Failed to fetch lines: " + res);
            String json = res.body().string();
            cacheService.put(cacheKey, json);
            return json;
        }
    }

    /** Obtiene todas las paradas de una línea como lista de Stop (uses cache) */
    public List<Stop> getStops(String lineId) throws IOException {
         String cacheKey = "stops:line:" + lineId;
         String cached = cacheService.get(cacheKey);
         if (cached != null) {
             JsonArray arr = JsonParser.parseString(cached).getAsJsonArray();
             List<Stop> stops = new ArrayList<>();
             for (JsonElement el : arr) {
                 stops.add(Stop.fromJson(el));
             }
             return stops;
         }

        // Fetch from both networks (TRAMBESOS=1 and TRAMBAIX=2) and combine results
        JsonArray combined = new JsonArray();
        String[] networkIds = new String[]{"1", "2"};
        for (String nid : networkIds) {
            int page = 0;
            while (true) {
                Request req = new Request.Builder()
                        .url(BASE_URL + "/lines/" + lineId + "/stops?page=" + page + "&pageSize=" + DEFAULT_PAGE_SIZE + "&networkId=" + nid)
                        .build();
                try (Response res = executeRequest(req)) {
                    if (!res.isSuccessful() || res.body() == null)
                        throw new IOException("Failed to fetch stops for line " + lineId + " networkId=" + nid + ": " + res);

                    String json = res.body().string();
                    JsonElement root = JsonParser.parseString(json);
                    JsonArray arr = normalizeToArray(root);
                    if (arr.isEmpty()) break;
                    for (JsonElement e : arr) combined.add(e);
                    if (arr.size() < DEFAULT_PAGE_SIZE) break;
                    page++;
                }
            }
        }

        List<Stop> stops = new ArrayList<>();
        // Deduplicate by gtfsCode (prefer), fallback to id
        java.util.Map<String, Stop> uniq = new java.util.LinkedHashMap<>();
        for (JsonElement o : combined) {
            Stop s = Stop.fromJson(o);
            if (s == null) continue;
            String key = null;
            if (s.getGtfsCode() != null && !s.getGtfsCode().isEmpty()) key = s.getGtfsCode().toUpperCase();
            else if (s.getId() != 0) key = String.valueOf(s.getId());
            else key = java.util.UUID.randomUUID().toString();
            if (!uniq.containsKey(key)) uniq.put(key, s);
        }
        stops.addAll(uniq.values());
        cacheService.put(cacheKey, gson.toJson(stops));
        AOBLogger.log("Fetched " + stops.size() + " stops for line " + lineId);
        if (!stops.isEmpty()) AOBLogger.log("Sample stop: " + stops.get(0).toJson());
        return stops;
    }

    /** Obtiene todas las paradas del sistema (cached) */
    public List<Stop> getAllStops() throws IOException {
         String cacheKey = "stops:all";
         String cached = cacheService.get(cacheKey);
         if (cached != null) {
             JsonArray arr = JsonParser.parseString(cached).getAsJsonArray();
             List<Stop> stops = new ArrayList<>();
             for (JsonElement el : arr) stops.add(Stop.fromJson(el));
             return stops;
         }

        // Fetch stops for both networks and combine
        JsonArray combined = new JsonArray();
        String[] networkIds = new String[]{"1", "2"};
        for (String nid : networkIds) {
            int page = 0;
            while (true) {
                Request req = new Request.Builder()
                        .url(BASE_URL + "/stops?page=" + page + "&pageSize=" + DEFAULT_PAGE_SIZE + "&networkId=" + nid)
                        .build();
                try (Response res = executeRequest(req)) {
                    if (!res.isSuccessful() || res.body() == null) {
                        throw new IOException("Failed to fetch stops for networkId=" + nid + ": " + res);
                    }

                    String json = res.body().string();
                    JsonElement root = JsonParser.parseString(json);
                    JsonArray arr = normalizeToArray(root);
                    if (arr.isEmpty()) break;
                    for (JsonElement e : arr) combined.add(e);
                    if (arr.size() < DEFAULT_PAGE_SIZE) break;
                    page++;
                }
            }
        }

        // Deduplicate by gtfsCode/id and produce list
        java.util.Map<String, Stop> uniqAll = new java.util.LinkedHashMap<>();
        for (JsonElement el : combined) {
            Stop s = Stop.fromJson(el);
            if (s == null) continue;
            String key = null;
            if (s.getGtfsCode() != null && !s.getGtfsCode().isEmpty()) key = s.getGtfsCode().toUpperCase();
            else if (s.getId() != 0) key = String.valueOf(s.getId());
            else key = java.util.UUID.randomUUID().toString();
            if (!uniqAll.containsKey(key)) uniqAll.put(key, s);
        }
        List<Stop> stops = new ArrayList<>(uniqAll.values());

        cacheService.put(cacheKey, gson.toJson(stops));
        AOBLogger.log("Fetched all stops: " + stops.size());
        if (!stops.isEmpty()) AOBLogger.log("Sample stop (all): " + stops.get(0).toJson());
        return stops;
    }

    /** Obtiene una parada individual por ID o por gtfs code */
    public Stop getStop(String stopId) throws IOException {
        if (stopId == null) return null;
        String query = stopId.trim();
        if (query.isEmpty()) return null;

        String qUpper = query.toUpperCase();

        List<Stop> all = getAllStops();

        // 1) exact numeric id match
        for (Stop s : all) {
            if (s == null) continue;
            if (s.getId() != 0) {
                try { if (Integer.parseInt(query) == s.getId()) return s; } catch (NumberFormatException ignored) {}
            }
        }

        // 2) exact gtfs code match
        for (Stop s : all) {
            if (s == null) continue;
            if (s.getGtfsCode() != null && s.getGtfsCode().trim().equalsIgnoreCase(query)) return s;
            if (s.getGtfsCode() != null && s.getGtfsCode().trim().toUpperCase().equals(qUpper)) return s;
        }

        // 3) sanitized match: remove non-alphanumeric and compare
        String sanitize = qUpper.replaceAll("[^A-Z0-9]", "");
        if (!sanitize.equals(qUpper)) {
            for (Stop s : all) {
                if (s == null || s.getGtfsCode() == null) continue;
                String sSan = s.getGtfsCode().trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (sSan.equals(sanitize)) return s;
            }
        }

        // 4) partial contains/startsWith (case-insensitive)
        for (Stop s : all) {
            if (s == null) continue;
            if (s.getGtfsCode() != null) {
                String code = s.getGtfsCode().toUpperCase();
                if (code.contains(qUpper) || code.startsWith(qUpper) || code.endsWith(qUpper)) return s;
            }
            if (s.getName() != null) {
                String name = s.getName().toUpperCase();
                if (name.contains(qUpper)) return s;
            }
        }

        AOBLogger.log("Stop not found for gtfsCode: " + query);
        AOBLogger.log("Available codes sample: " + listAllGtfsCodesString(10));
        return null;
    }

    /** Return a set of all GTFS codes (for debugging). */
    public Set<String> listAllGtfsCodes() throws IOException {
        List<Stop> stops = getAllStops();
        Set<String> codes = new HashSet<>();
        for (Stop s : stops) if (s != null && s.getGtfsCode() != null && !s.getGtfsCode().isEmpty()) codes.add(s.getGtfsCode());
        return codes;
    }

    private String listAllGtfsCodesString(int limit) throws IOException {
        Set<String> codes = listAllGtfsCodes();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String c : codes) {
            if ( i++ >= limit) break;
            if (sb.length() > 0) sb.append(", ");
            sb.append(c);
        }
        return sb.toString();
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

        FeedMessage.Builder merged = FeedMessage.newBuilder();
        try (Response tripRes = executeRequest(tripReq);
             Response vehRes = executeRequest(vehicleReq)) {

            if (!tripRes.isSuccessful() || tripRes.body() == null)
                throw new IOException("Failed to fetch trip feed: " + tripReq);
            if (!vehRes.isSuccessful() || vehRes.body() == null)
                throw new IOException("Failed to fetch vehicle feed: " + vehicleReq);

            FeedMessage tripFeed = FeedMessage.parseFrom(tripRes.body().bytes());
            FeedMessage vehFeed = FeedMessage.parseFrom(vehRes.body().bytes());

            merged.addAllEntity(tripFeed.getEntityList());
            merged.addAllEntity(vehFeed.getEntityList());
        }

        FeedHeader header = FeedHeader.newBuilder()
                .setGtfsRealtimeVersion("2")
                .setTimestamp(System.currentTimeMillis() / 1000)
                .build();
        merged.setHeader(header);

        FeedMessage feed = merged.build();
        cacheService.putGtfsRt(cacheKey, feed, 30);
        return feed;
    }

    /**
     * Collect a set of all GTFS stop identifiers found in the static stop list (gtfsCode or id).
     */
    public Set<String> getAllStopIds() throws IOException {
        List<Stop> stops = getAllStops();
        Set<String> ids = new HashSet<>();
        for (Stop s : stops) {
            if (s.getGtfsCode() != null && !s.getGtfsCode().isEmpty()) ids.add(s.getGtfsCode());
            // also add numeric id as string
            if (s.getId() != 0) ids.add(String.valueOf(s.getId()));
        }
        return ids;
    }

    /**
     * Find stop ids present in GTFS-RT but not in static stops (useful to detect mismatches).
     */
    public Set<String> findMissingStaticStopsInGtfsRt(String network) throws IOException {
         Set<String> staticIds = getAllStopIds();
         FeedMessage feed = getGtfsrtData(network);
         Set<String> rtIds = new HashSet<>();
         for (FeedEntity entity : feed.getEntityList()) {
             if (!entity.hasTripUpdate()) continue;
             TripUpdate trip = entity.getTripUpdate();
             for (TripUpdate.StopTimeUpdate stu : trip.getStopTimeUpdateList()) {
                 if (stu.hasStopId()) rtIds.add(stu.getStopId());
             }
         }
         rtIds.removeAll(staticIds);
         return rtIds;
     }

    /** Clear API-related caches (stops/lines) by clearing the CacheService. Use with care. */
    public void clearAllApiCache() {
         cacheService.clear();
         AOBLogger.log("CacheService cleared by TramApiClient.clearAllApiCache()");
     }

    /** Return the raw cached JSON for all stops (or null if not cached). Useful for debugging. */
    public String getStopsCache() {
        return cacheService.get("stops:all");
    }

    /**
     * Execute a request with a small retry/backoff policy for transient network errors.
     * The caller is responsible for closing the returned Response.
     */
    private Response executeRequest(Request req) throws IOException {
        int attempt = 0;
        long backoff = HTTP_BACKOFF_MS;
        while (true) {
            attempt++;
            try {
                return client.newCall(req).execute();
            } catch (IOException e) {
                if (attempt >= HTTP_MAX_RETRIES) throw e;
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry backoff", ie);
                }
                backoff *= 2;
            }
        }
    }

    /**
     * Normalize a JsonElement into a JsonArray.
     * If the element is already an array, return it.
     * If it's an object, try common array container keys (data, stops, items, results).
     * If it's a single object, wrap it into a one-element array.
     */
    private JsonArray normalizeToArray(JsonElement root) {
        JsonArray arr = new JsonArray();
        if (root == null || root.isJsonNull()) return arr;
        if (root.isJsonArray()) return root.getAsJsonArray();
        if (root.isJsonObject()) {
            var obj = root.getAsJsonObject();
            String[] keys = new String[]{"data", "stops", "items", "results", "features"};
            for (String k : keys) {
                if (obj.has(k) && obj.get(k).isJsonArray()) return obj.get(k).getAsJsonArray();
            }
            // some APIs return an object with numeric keys or a single object representing a stop
            // try to find a nested array anywhere
            for (var entry : obj.entrySet()) {
                if (entry.getValue().isJsonArray()) return entry.getValue().getAsJsonArray();
            }
            // fallback: wrap the object itself
            arr.add(root);
            return arr;
        }
        // primitive -> treat as single value
        arr.add(root);
        return arr;
    }
}
