package dev.x341.aonbas2srv.services;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dev.x341.aonbas2srv.services.apiclients.TmbApiClient;
import dev.x341.aonbas2srv.util.AOBConfig;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AOBConfig.class).in(Singleton.class);

        bind(TmbApiClient.class).in(Singleton.class);

        bind(CacheService.class).in(Singleton.class);

        bind(MetroService.class).in(Singleton.class);

        bind(TramService.class).in(Singleton.class);

        bind(HttpServerHandler.class);
    }
}
