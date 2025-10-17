package dev.x341.aonbas2srv.services;

import com.google.inject.Inject;
import dev.x341.aonbas2srv.util.AOBLogger;

import java.io.IOException;

/**
 * MetroService provides a thin application-level facade over the TMB API client.
 * It catches IOExceptions from the API client, logs them, and returns a small JSON
 * error payload so callers (HTTP handlers) can return consistent error responses.
 */
public class MetroService {
    private final TmbApiClient tmbApiClient;

    @Inject
    public MetroService(TmbApiClient tmbApiClient) {
        this.tmbApiClient = tmbApiClient;
    }

    /**
     * Retrieve the list of metro lines as a JSON string.
     * @return JSON string with metro lines or an error JSON when the API call fails
     */
    public String getLines() {
        try {
            return tmbApiClient.getMetroLines();
        } catch (IOException e) {
            AOBLogger.error("Error getting metro lines", e);
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Retrieve stations for a given line code.
     * @param lineCode line identifier (e.g. "L1")
     * @return JSON string with stations or an error JSON when the API call fails
     */
    public String getStationForLine(String lineCode) {
        try {
            return tmbApiClient.getStationOnLine(lineCode);
        } catch (IOException e) {
            AOBLogger.error("Error getting stations for line " + lineCode, e);
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Retrieve train arrival times for a station.
     * @param stationCode station identifier
     * @return JSON string with train times or an error JSON when the API call fails
     */
    public String getTrainTimes(String stationCode) {
        try {
            return tmbApiClient.getTrainsForStation(stationCode);
        } catch (IOException e) {
            AOBLogger.error("Error getting trains for station " + stationCode, e);
            return formatError("API_ERROR", e);
        }
    }

    /**
     * Helper to produce a small JSON error object. Centralizes the format used by the service.
     * The JSON produced has two fields: "error" and "message".
     *
     * @param errorCode short error code
     * @param e the exception that caused the error (message is used in the payload)
     * @return JSON string with error details
     */
    private String formatError(String errorCode, Exception e) {
        String msg = e == null ? "" : e.getMessage();
        // produce: {"error":"API_ERROR","message":"..."}
        return String.format("{\"error\":\"%s\",\"message\":\"%s\"}", errorCode, escapeJson(msg));
    }

    /**
     * Minimal string escaper for JSON string values. This escapes backslashes and double quotes
     * so the produced JSON remains valid for typical error messages.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
