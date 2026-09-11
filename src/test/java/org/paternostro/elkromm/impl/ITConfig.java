package org.paternostro.elkromm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ITConfig {
    public static final Logger logger       = LoggerFactory.getLogger(ITConfig.class);

    public static final String CONFIG_FILE  = "elkron-it.properties";
    public static final String HOST         = "org.paternostro.elkromm.impl.IT.host";
    public static final String PORT         = "org.paternostro.elkromm.impl.IT.port";
    public static final String PLANT_CODE   = "org.paternostro.elkromm.impl.IT.plantCode";
    public static final String TECH_PIN     = "org.paternostro.elkromm.impl.IT.technicalPin";

    protected static ITConfig instance      = null;

    public synchronized static ITConfig getInstance() {
        if (instance == null) {
            instance = new ITConfig();
        }

        return instance;
    }

    protected Properties properties;

    private ITConfig() {
        properties = new Properties();

        try {
            logger.info("Loading configuration from " + CONFIG_FILE);
            InputStream stream = this.getClass().getResourceAsStream(CONFIG_FILE);

            if (stream != null) {
                properties.load(stream);
            } else {
                logger.warn("Configuration file " + CONFIG_FILE + " not found, disabling integration tests");
                properties = null;
                return;
            }
        } catch (IOException e) {
            logger.warn("Error loading configuration from " + CONFIG_FILE + ", disabling integration tests", e);
            properties = null;
            return;
        }

        // Check values presence
        if (!properties.containsKey(HOST) || "".equals(properties.get(HOST))) {
            logger.warn("Missing " + HOST + " value, disabling integration tests");
            properties = null;
            return;
        }

        if (!properties.containsKey(PORT) || "".equals(properties.get(PORT))) {
            logger.warn("Missing " + PORT + " value, disabling integration tests");
            properties = null;
            return;
        }

        if (!properties.containsKey(PLANT_CODE) || "".equals(properties.get(PLANT_CODE))) {
            logger.warn("Missing " + PLANT_CODE + " value, disabling integration tests");
            properties = null;
            return;
        }

        if (!properties.containsKey(TECH_PIN) || "".equals(properties.get(TECH_PIN))) {
            logger.warn("Missing " + TECH_PIN + " value, disabling integration tests");
            properties = null;
            // return;
        }
    }

    public boolean areITEnabled() {
        return properties != null;
    }

    public String getHost() {
        return properties.getProperty(HOST);
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty(PORT));
    }

    public int getPlantCode() {
        return Integer.parseInt(properties.getProperty(PLANT_CODE));
    }

    public int getTechnicalPin() {
        return Integer.parseInt(properties.getProperty(TECH_PIN));
    }
}
