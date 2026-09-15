package org.paternostro.elkromm.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Config {
    public static final Logger logger       = LoggerFactory.getLogger(Config.class);

    public static final String CONFIG_FILE      = "elkron.properties";
    public static final String K_PORT           = "org.paternostro.elkron.port";
    public static final String D_PORT           = "8030";
    public static final String K_PLANT_CODE     = "org.paternostro.elkron.plant.code";
    public static final String D_PLANT_CODE     = "55555555";
    public static final String K_TECHNICAL_CODE = "org.paternostro.elkron.technical.code";
    public static final String D_TECHNICAL_CODE = "000000";
    public static final String K_KEYBOARDS      = "org.paternostro.elkron.keyboards";
    public static final String D_KEYBOARDS      = "0";
    public static final String K_READERS        = "org.paternostro.elkron.readers";
    public static final String D_READERS        = "0";
    public static final String K_EXPANSIONS     = "org.paternostro.elkron.expansions";
    public static final String D_EXPANSIONS     = "0";
    public static final String K_AREAS          = "org.paternostro.elkron.areas";
    public static final String D_AREAS          = "0";

    public static final String K_AREA_SECTORS   = "org.paternostro.elkron.area.%d.sectors";
    public static final String[] D_AREA_SECTORS = {"1", "0", "0", "0"};

    public static final String K_AREA_NAME      = "org.paternostro.elkron.area.%d.name";
    public static final String[] D_AREA_NAME    = {"...", ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME};
    
    public static final String K_SECTORS        = "org.paternostro.elkron.sectors";
    public static final String D_SECTORS        = "1";

    public static final String K_SECTOR_ENTRY_TIME    = "org.paternostro.elkron.sector.%d.entry.time";
    public static final String[] D_SECTOR_ENTRY_TIME  = {"0", "0", "0", "0", "0", "0", "0", "0"};

    public static final String K_SECTOR_EXIT_TIME     = "org.paternostro.elkron.sector.%d.exit.time";
    public static final String[] D_SECTOR_EXIT_TIME   = {"0", "0", "0", "0", "0", "0", "0", "0"};

    public static final String K_SECTOR_NAME    = "org.paternostro.elkron.sector.%d.name";
    public static final String[] D_SECTOR_NAME  = {ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME};

    public static final String K_SECTOR_TYPE    = "org.paternostro.elkron.sector.%d.type";
    public static final String[] D_SECTOR_TYPE  = {"STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD"};

    protected static Config instance = null;

    protected Properties properties;

    private Config() {
        this.properties = new Properties();

        try {
            logger.info("Loading configuration from " + CONFIG_FILE);
            InputStream stream = this.getClass().getResourceAsStream(CONFIG_FILE);

            if (stream != null) {
                properties.load(stream);
            } else {
                logger.warn("Configuration file " + CONFIG_FILE + " not found, using default values");
            }
        } catch (IOException e) {
            logger.warn("Error loading configuration from " + CONFIG_FILE + ", using default values", e);
        }
    }

    public synchronized static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }

        return instance;
    }

    protected int getIntProperty(String key, String defaultValue, String description) {
        try {
            return Integer.parseInt(properties.getProperty(key, defaultValue));
        } catch (NumberFormatException e) {
            logger.warn("Invalid " + description + " format in configuration, using default: " + defaultValue, e);
            return Integer.parseInt(defaultValue);
        }
    }

    public int getPort() {
        return getIntProperty(K_PORT, D_PORT, "port");
    }

    public int getPlantCode() {
        return getIntProperty(K_PLANT_CODE, D_PLANT_CODE, "plant code");
    }

    public int getTechnicalCode() {
        return getIntProperty(K_TECHNICAL_CODE, D_TECHNICAL_CODE, "technical code");
    }

    public int getKeyboards() {
        return getIntProperty(K_KEYBOARDS, D_KEYBOARDS, "keyboards");
    }

    public int getReaders() {
        return getIntProperty(K_READERS, D_READERS, "readers");
    }

    public int getExpansions() {
        return getIntProperty(K_EXPANSIONS, D_EXPANSIONS, "expansions");
    }

    public int getAreas() {
        return getIntProperty(K_AREAS, D_AREAS, "areas");
    }

    public int getAreaSectors(int area) {
        String  key,
                defaultValue;
        
        if (area < 1 || area > ElkrommFacade.MAX_AREAS) {
            logger.warn("Invalid area number: " + area + ", must be between 1 and " + ElkrommFacade.MAX_AREAS);
            return 0;
        }
        
        key = String.format(K_AREA_SECTORS, area);
        defaultValue = D_AREA_SECTORS[area - 1];

        return getIntProperty(key, defaultValue, "area " + area + " sectors");
    }

    public String getAreaName(int area) {
        String  key,
                defaultValue;
        
        if (area < 1 || area > ElkrommFacade.MAX_AREAS) {
            logger.warn("Invalid area number: " + area + ", must be between 1 and " + ElkrommFacade.MAX_AREAS);
            return "...";
        }
        
        key = String.format(K_AREA_NAME, area);
        defaultValue = D_AREA_NAME[area - 1];

        return properties.getProperty(key, defaultValue);
    }

    public int getSectors() {
        return getIntProperty(K_SECTORS, D_SECTORS, "sectors");
    }

    public int getSectorEntryTime(int sector) {
        String  key,
                defaultValue;
        
        if (sector < 1 || sector > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid sector number: " + sector + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return 0;
        }
        
        key = String.format(K_SECTOR_ENTRY_TIME, sector);
        defaultValue = D_SECTOR_ENTRY_TIME[sector - 1];

        return getIntProperty(key, defaultValue, "sector " + sector + " entry time");
    }

    public int getSectorExitTime(int sector) {
        String  key,
                defaultValue;
        
        if (sector < 1 || sector > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid sector number: " + sector + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return 0;
        }
        
        key = String.format(K_SECTOR_EXIT_TIME, sector);
        defaultValue = D_SECTOR_EXIT_TIME[sector - 1];
        
        return getIntProperty(key, defaultValue, "sector " + sector + " exit time");
    }

    public String getSectorName(int sector) {
        String  key,
                defaultValue;
        
        if (sector < 1 || sector > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid sector number: " + sector + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return "...";
        }
        
        key = String.format(K_SECTOR_NAME, sector);
        defaultValue = D_SECTOR_NAME[sector - 1];
        
        return properties.getProperty(key, defaultValue);
    }

    public Partition.Type getSectorType(int sector) {
        String  key,
                defaultValue;
        
        if (sector < 1 || sector > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid sector number: " + sector + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return null;
        }
        
        key = String.format(K_SECTOR_TYPE, sector);
        defaultValue = D_SECTOR_TYPE[sector - 1];
        
        return Partition.Type.valueOf(properties.getProperty(key, defaultValue));
    }
}
