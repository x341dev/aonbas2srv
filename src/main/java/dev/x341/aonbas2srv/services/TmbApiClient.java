package dev.x341.aonbas2srv.services;

import com.google.inject.Inject;
import dev.x341.aonbas2srv.util.AOBConfig;
import dev.x341.aonbas2srv.util.AOBLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class TmbApiClient {
    private final OkHttpClient client;
    private final String authParams;
    private static final String BASE_URL = "https://api.tmb.cat/v1";

    @Inject
    public TmbApiClient(AOBConfig config) {
        this.client = new OkHttpClient();
        this.authParams = String.format("app_id=%s&app_key=%s", config.getAppId(), config.getAppKey());
    }

    /**
     * Build a full URL for an endpoint under the TMB base API, automatically appending auth params.
     * Example: buildUrl("transit/linies/metro") -> https://api.tmb.cat/v1/transit/linies/metro?app_id=...&app_key=...
     *
     * @param endpoint endpoint path relative to the base API (no leading slash)
     * @return fully-qualified URL with authentication query parameters
     */
    private String buildUrl(String endpoint) {
        return String.format("%s/%s?%s", BASE_URL, endpoint, authParams);
    }

    /**
     * Execute a GET request against the provided fully-qualified URL and return the response body as string.
     * Commonizes the request/response handling so callers don't duplicate okhttp boilerplate.
     *
     * @param url fully-qualified URL to call
     * @return response body as string
     * @throws IOException if the HTTP call fails or returns a non-success status
     */
    private String executeGetUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        AOBLogger.log("Calling TMB API: " + url);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("TMB API call failed: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    /**
     * Execute a TMB API call for an endpoint relative to the base URL.
     * This will automatically build the URL (including auth params) and perform the GET.
     *
     * @param endpoint endpoint path relative to the base API (no leading slash)
     * @return response body as string
     * @throws IOException if the HTTP call fails
     */
    private String executeTmbCall(String endpoint) throws IOException {
        String url = buildUrl(endpoint);
        return executeGetUrl(url);
    }

    /**
     * Get the list of metro lines from the TMB API.
     * @return JSON string representing metro lines
     * @throws IOException when the API call fails
     */
    public String getMetroLines() throws IOException {
        return executeTmbCall("transit/linies/metro");
    }

    /**
     * Get stations for the specified metro line.
     * @param lineCode the line code (e.g. L1, L2)
     * @return JSON string with stations for the line
     * @throws IOException when the API call fails
     */
    public String getStationOnLine(String lineCode) throws IOException {
        String endpoint = String.format("transit/linies/metro/%s/estacions", lineCode);
        return executeTmbCall(endpoint);
    }

    /**
     * Get interchanges (correspondences) for a given line and station.
     * @param lineCode the metro line code
     * @param stationCode the station code
     * @return JSON string with interchange information
     * @throws IOException when the API call fails
     */
    public String getInterchanges(String lineCode, String stationCode) throws IOException {
        String endpoint = String.format("transit/linies/metro/%s/estacions/%s/corresp", lineCode, stationCode);
        return executeTmbCall(endpoint);
    }

    /**
     * Get train arrival times for a specific station. This endpoint uses a different path/query format,
     * so we build the full URL and use the common executeGetUrl helper.
     *
     * @param stationCode station identifier to query
     * @return JSON string with trains information
     * @throws IOException when the API call fails
     */
    public String getTrainsForStation(String stationCode) throws IOException {
        String url = String.format("%s/itransit/metro/estacions?estacions=%s&%s", BASE_URL, stationCode, authParams);
        return executeGetUrl(url);
    }

}
