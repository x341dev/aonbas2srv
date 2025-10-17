package dev.x341.aonbas2srv.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AOBLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("AONBAS2SRV");

    public static void log(String msg) {
        LOGGER.info(msg);
    }

    public static void error(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }
}
