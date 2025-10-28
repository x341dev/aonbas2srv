package dev.x341.aonbas2srv.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Stop {
    private String name;
    private String description;
    private double latitude;
    private double longitude;
    private Integer outboundCode;
    private Integer returnCode;
    private String gtfsCode;
    private Integer order;
    private String image;
    private Integer id;

    // --- Getters y Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Integer getOutboundCode() { return outboundCode; }
    public void setOutboundCode(Integer outboundCode) { this.outboundCode = outboundCode; }
    public Integer getReturnCode() { return returnCode; }
    public void setReturnCode(Integer returnCode) { this.returnCode = returnCode; }
    public String getGtfsCode() { return gtfsCode; }
    public void setGtfsCode(String gtfsCode) { this.gtfsCode = gtfsCode; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    // --- Parse flexible JSON ---
    public static Stop fromJson(JsonElement el) {
        Stop stop = new Stop();
        try {
            JsonObject obj = el.isJsonObject() ? el.getAsJsonObject() : JsonParser.parseString(el.getAsString()).getAsJsonObject();

            stop.name = getString(obj, "name");
            stop.description = extractDescription(obj, "description");

            stop.latitude = getDouble(obj, "latitude", "lat");
            stop.longitude = getDouble(obj, "longitude", "lon", "lng");

            stop.outboundCode = getInt(obj, "outboundCode", "outbound_code");
            stop.returnCode = getInt(obj, "returnCode", "return_code");

            stop.gtfsCode = getString(obj, "gtfsCode", "gtfs_id", "gtfsId", "code");
            stop.order = getInt(obj, "order");
            stop.image = getString(obj, "image", "img");
            stop.id = getInt(obj, "id", "stopId");
        } catch (Exception ignored) {
            try { return new Gson().fromJson(el, Stop.class); } catch (Exception ignore) {}
        }
        return stop;
    }

    public static Stop fromJson(String json) {
        return fromJson(JsonParser.parseString(json));
    }

    // --- Helpers ---
    private static String getString(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) return obj.get(k).getAsString();
        }
        return null;
    }

    private static Integer getInt(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                try { return obj.get(k).getAsInt(); }
                catch (Exception e) {
                    try { return Integer.parseInt(obj.get(k).getAsString()); } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    private static double getDouble(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                try { return obj.get(k).getAsDouble(); } catch (Exception ignored) {}
            }
        }
        return 0;
    }

    private static String extractDescription(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
        JsonElement el = obj.get(key);
        if (el.isJsonPrimitive()) return el.getAsString();
        if (el.isJsonObject()) {
            JsonObject dobj = el.getAsJsonObject();
            String[] langs = {"ca","es","en","text","value"};
            for (String l : langs) if (dobj.has(l) && !dobj.get(l).isJsonNull()) return dobj.get(l).getAsString();
            for (String prop : dobj.keySet()) try { return dobj.get(prop).getAsString(); } catch (Exception ignored) {}
        }
        return el.toString();
    }

    // --- Convert to JSON ---
    public String toJson() {
        return new Gson().toJson(this);
    }
}
