package dev.x341.aonbas2srv.util;

import com.google.inject.Singleton;
import io.github.cdimascio.dotenv.Dotenv;

@Singleton
public class AOBConfig {
    private final String tmbAppId;
    private final String tmbAppKey;

    private final String tramClientId;
    private final String tramClientSecret;

    public AOBConfig() {
        Dotenv dotenv = Dotenv.load();
        this.tmbAppId = dotenv.get("TMB_APP_ID");
        this.tmbAppKey = dotenv.get("TMB_APP_KEY");

        this.tramClientId = dotenv.get("TRAM_CLIENT_ID");
        this.tramClientSecret = dotenv.get("TRAM_CLIENT_SECRET");

        if (this.tmbAppId == null || this.tmbAppKey == null) {
            throw new RuntimeException("Missing TMB_APP_ID or TMB_APP_KEY in .env file.");
        }

        if (this.tramClientId == null || this.tramClientSecret == null) {
            throw new RuntimeException("Missing TRAM_CLIENT_ID or TRAM_CLIENT_SECRET in .env file.");
        }
    }

    public String getTmbAppId() { return tmbAppId; };
    public String getTmbAppKey() { return tmbAppKey; };

    public String getTramClientId() { return tramClientId; };
    public String getTramClientSecret() { return  tramClientSecret; };
}
