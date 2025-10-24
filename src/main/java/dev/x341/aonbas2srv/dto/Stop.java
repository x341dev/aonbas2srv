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
    private int outboundCode;
    private int returnCode;
    private String gtfsCode;
    private Integer order;
    private String image;
    private int id;

    // Getters y setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getOutboundCode() { return outboundCode; }
    public int getReturnCode() { return returnCode; }
    public String getGtfsCode() { return gtfsCode; }
    public Integer getOrder() { return order; }
    public String getImage() { return image; }
    public int getId() { return id; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setOutboundCode(int outboundCode) { this.outboundCode = outboundCode; }
    public void setReturnCode(int returnCode) { this.returnCode = returnCode; }
    public void setGtfsCode(String gtfsCode) { this.gtfsCode = gtfsCode; }
    public void setOrder(Integer order) { this.order = order; }
    public void setImage(String image) { this.image = image; }
    public void setId(int id) { this.id = id; }

    /**
     * Parse a Stop from a JSON string.
     * This is tolerant to fields that may be objects instead of primitive strings (e.g. description).
     */
    public static Stop fromJson(String o) {
        JsonElement el = JsonParser.parseString(o);
        return fromJson(el);
    }

    /**
     * Parse a Stop from a Gson JsonElement.
     * Handles flexible JSON shapes (description may be an object or a string, field name variants, etc.).
     */
    public static Stop fromJson(JsonElement el) {
        Stop s = new Stop();
        try {
            JsonObject obj = el.isJsonObject() ? el.getAsJsonObject() : JsonParser.parseString(el.getAsString()).getAsJsonObject();

            // name
            if (obj.has("name") && !obj.get("name").isJsonNull()) s.setName(obj.get("name").getAsString());

            // description: may be string or object -> try common nested fields
            if (obj.has("description") && !obj.get("description").isJsonNull()) {
                JsonElement descEl = obj.get("description");
                if (descEl.isJsonPrimitive()) {
                    String raw = descEl.getAsString();
                    // sometimes the API returns a JSON object encoded as string: "{\"ca\":...}"
                    if (raw != null && raw.startsWith("{")) {
                        try {
                            JsonObject dobj = JsonParser.parseString(raw).getAsJsonObject();
                            s.setDescription(extractLocalizedDescription(dobj));
                        } catch (Exception ex) {
                            s.setDescription(raw);
                        }
                    } else {
                        s.setDescription(raw);
                    }
                } else if (descEl.isJsonObject()) {
                    JsonObject dobj = descEl.getAsJsonObject();
                    if (dobj.has("text") && !dobj.get("text").isJsonNull()) s.setDescription(dobj.get("text").getAsString());
                    else if (dobj.has("value") && !dobj.get("value").isJsonNull()) s.setDescription(dobj.get("value").getAsString());
                    else s.setDescription(dobj.toString());
                } else {
                    s.setDescription(descEl.toString());
                }
            }

            // latitude / longitude (multiple possible field names)
            if (obj.has("latitude") && !obj.get("latitude").isJsonNull()) s.setLatitude(obj.get("latitude").getAsDouble());
            else if (obj.has("lat") && !obj.get("lat").isJsonNull()) s.setLatitude(obj.get("lat").getAsDouble());

            if (obj.has("longitude") && !obj.get("longitude").isJsonNull()) s.setLongitude(obj.get("longitude").getAsDouble());
            else if (obj.has("lon") && !obj.get("lon").isJsonNull()) s.setLongitude(obj.get("lon").getAsDouble());
            else if (obj.has("lng") && !obj.get("lng").isJsonNull()) s.setLongitude(obj.get("lng").getAsDouble());

            // outbound/return codes
            if (obj.has("outboundCode") && !obj.get("outboundCode").isJsonNull()) s.setOutboundCode(obj.get("outboundCode").getAsInt());
            else if (obj.has("outbound_code") && !obj.get("outbound_code").isJsonNull()) s.setOutboundCode(obj.get("outbound_code").getAsInt());

            if (obj.has("returnCode") && !obj.get("returnCode").isJsonNull()) s.setReturnCode(obj.get("returnCode").getAsInt());
            else if (obj.has("return_code") && !obj.get("return_code").isJsonNull()) s.setReturnCode(obj.get("return_code").getAsInt());

            // gtfs code variants
            if (obj.has("gtfsCode") && !obj.get("gtfsCode").isJsonNull()) s.setGtfsCode(obj.get("gtfsCode").getAsString());
            else if (obj.has("gtfs_id") && !obj.get("gtfs_id").isJsonNull()) s.setGtfsCode(obj.get("gtfs_id").getAsString());
            else if (obj.has("gtfsId") && !obj.get("gtfsId").isJsonNull()) s.setGtfsCode(obj.get("gtfsId").getAsString());
            else if (obj.has("code") && !obj.get("code").isJsonNull()) s.setGtfsCode(obj.get("code").getAsString());

            // order
            if (obj.has("order") && !obj.get("order").isJsonNull()) s.setOrder(obj.get("order").getAsInt());

            // image
            if (obj.has("image") && !obj.get("image").isJsonNull()) s.setImage(obj.get("image").getAsString());
            else if (obj.has("img") && !obj.get("img").isJsonNull()) s.setImage(obj.get("img").getAsString());

            // id
            if (obj.has("id") && !obj.get("id").isJsonNull()) {
                try { s.setId(obj.get("id").getAsInt()); } catch (Exception ex) {
                    try { s.setId(Integer.parseInt(obj.get("id").getAsString())); } catch (Exception ignore) {}
                }
            } else if (obj.has("stopId") && !obj.get("stopId").isJsonNull()) {
                try { s.setId(obj.get("stopId").getAsInt()); } catch (Exception ex) {
                    try { s.setId(Integer.parseInt(obj.get("stopId").getAsString())); } catch (Exception ignore) {}
                }
            }

        } catch (Exception e) {
            // Fallback: attempt to deserialize with Gson for any fields we didn't handle
            try {
                Stop fallback = new Gson().fromJson(el, Stop.class);
                if (fallback != null) return fallback;
            } catch (Exception ignored) {}
        }

        return s;
    }

    /**
     * Pick a localized description from an object that may contain language keys.
     * Preference order: ca, es, en, any first available value.
     */
    private static String extractLocalizedDescription(JsonObject dobj) {
        if (dobj == null) return null;
        String[] keys = new String[]{"ca", "es", "en", "text", "value"};
        for (String k : keys) {
            if (dobj.has(k) && !dobj.get(k).isJsonNull()) {
                try { return dobj.get(k).getAsString(); } catch (Exception ignored) {}
            }
        }
        // fallback: return first property's string value
        for (String prop : dobj.keySet()) {
            try { return dobj.get(prop).getAsString(); } catch (Exception ignored) {}
        }
        return dobj.toString();
    }

    // Convierte la parada a JSON
    public String toJson() {
        return new Gson().toJson(this);
    }
}
