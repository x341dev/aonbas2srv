package dev.x341.aonbas2srv.dto;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * OtpDto is a small transfer object intended to be language-agnostic when serialized as JSON.
 * Fields are simple primitives/strings so they are easy to consume from Kotlin Multiplatform.
 *
 * Example JSON: {"id":"abc123","type":"station","payload":"...","ts":163...}
 */
public class OtpDto {
    private static final Gson GSON = new Gson();

    @SerializedName("id")
    private final String id;

    @SerializedName("type")
    private final String type;

    @SerializedName("payload")
    private final String payload;

    @SerializedName("ts")
    private final long timestamp;

    public OtpDto(String id, String type, String payload, long timestamp) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getPayload() { return payload; }
    public long getTimestamp() { return timestamp; }

    /**
     * Serialize this DTO to a compact JSON string.
     * @return JSON representation
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * Convert the JSON representation to a UTF-8 byte array. Useful for binary transports.
     * @return UTF-8 bytes of the JSON representation
     */
    public byte[] toBytes() {
        return toJson().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Create an OtpDto instance from a JSON string.
     * @param json JSON produced by toJson()
     * @return OtpDto instance
     */
    public static OtpDto fromJson(String json) {
        return GSON.fromJson(json, OtpDto.class);
    }

    /**
     * Create an OtpDto instance from a UTF-8 byte array containing JSON.
     * @param bytes UTF-8 bytes of JSON
     * @return OtpDto instance
     */
    public static OtpDto fromBytes(byte[] bytes) {
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        return fromJson(json);
    }
}

