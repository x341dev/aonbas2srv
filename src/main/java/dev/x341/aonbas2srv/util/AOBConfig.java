package dev.x341.aonbas2srv.util;

import com.google.inject.Singleton;
import io.github.cdimascio.dotenv.Dotenv;

@Singleton
public class AOBConfig {
    private final String appId;
    private final String appKey;

    public AOBConfig() {
        Dotenv dotenv = Dotenv.load();
        this.appId = dotenv.get("TMB_APP_ID");
        this.appKey = dotenv.get("TMB_APP_KEY");

        if (this.appId == null || this.appKey == null) {
            throw new RuntimeException("Missing TMB_APP_ID or TMB_APP_KEY in .env file.");
        }
    }

    public String getAppId() { return appId; };
    public String getAppKey() { return appKey; };

}
