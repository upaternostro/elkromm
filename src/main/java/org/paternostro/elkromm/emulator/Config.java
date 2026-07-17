package org.paternostro.elkromm.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.paternostro.elkromm.dto.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final String K_AREA_1_SECTORS = "org.paternostro.elkron.area.1.sectors";
    public static final String D_AREA_1_SECTORS = "1";
    public static final String K_AREA_2_SECTORS = "org.paternostro.elkron.area.2.sectors";
    public static final String D_AREA_2_SECTORS = "0";
    public static final String K_AREA_3_SECTORS = "org.paternostro.elkron.area.3.sectors";
    public static final String D_AREA_3_SECTORS = "0";
    public static final String K_AREA_4_SECTORS = "org.paternostro.elkron.area.4.sectors";
    public static final String D_AREA_4_SECTORS = "0";
    public static final String K_AREA_1_NAME    = "org.paternostro.elkron.area.1.name";
    public static final String D_AREA_1_NAME    = "...";
    public static final String K_AREA_2_NAME    = "org.paternostro.elkron.area.2.name";
    public static final String D_AREA_2_NAME    = "...                     ";
    public static final String K_AREA_3_NAME    = "org.paternostro.elkron.area.3.name";
    public static final String D_AREA_3_NAME    = "...                     ";
    public static final String K_AREA_4_NAME    = "org.paternostro.elkron.area.4.name";
    public static final String D_AREA_4_NAME    = "...                     ";
    public static final String K_SECTORS        = "org.paternostro.elkron.sectors";
    public static final String D_SECTORS        = "1";
    public static final String K_SECTOR_1_ENTRY_TIME  = "org.paternostro.elkron.sector.1.entry.time";
    public static final String D_SECTOR_1_ENTRY_TIME  = "0";
    public static final String K_SECTOR_2_ENTRY_TIME  = "org.paternostro.elkron.sector.2.entry.time";
    public static final String D_SECTOR_2_ENTRY_TIME  = "30";
    public static final String K_SECTOR_3_ENTRY_TIME  = "org.paternostro.elkron.sector.3.entry.time";
    public static final String D_SECTOR_3_ENTRY_TIME  = "0";
    public static final String K_SECTOR_4_ENTRY_TIME  = "org.paternostro.elkron.sector.4.entry.time";
    public static final String D_SECTOR_4_ENTRY_TIME  = "0";
    public static final String K_SECTOR_5_ENTRY_TIME  = "org.paternostro.elkron.sector.5.entry.time";
    public static final String D_SECTOR_5_ENTRY_TIME  = "0";
    public static final String K_SECTOR_6_ENTRY_TIME  = "org.paternostro.elkron.sector.6.entry.time";
    public static final String D_SECTOR_6_ENTRY_TIME  = "0";
    public static final String K_SECTOR_7_ENTRY_TIME  = "org.paternostro.elkron.sector.7.entry.time";
    public static final String D_SECTOR_7_ENTRY_TIME  = "0";
    public static final String K_SECTOR_8_ENTRY_TIME  = "org.paternostro.elkron.sector.8.entry.time";
    public static final String D_SECTOR_8_ENTRY_TIME  = "0";
    public static final String K_SECTOR_1_EXIT_TIME   = "org.paternostro.elkron.sector.1.exit.time";
    public static final String D_SECTOR_1_EXIT_TIME   = "0";
    public static final String K_SECTOR_2_EXIT_TIME   = "org.paternostro.elkron.sector.2.exit.time";
    public static final String D_SECTOR_2_EXIT_TIME   = "30";
    public static final String K_SECTOR_3_EXIT_TIME   = "org.paternostro.elkron.sector.3.exit.time";
    public static final String D_SECTOR_3_EXIT_TIME   = "0";
    public static final String K_SECTOR_4_EXIT_TIME   = "org.paternostro.elkron.sector.4.exit.time";
    public static final String D_SECTOR_4_EXIT_TIME   = "0";
    public static final String K_SECTOR_5_EXIT_TIME   = "org.paternostro.elkron.sector.5.exit.time";
    public static final String D_SECTOR_5_EXIT_TIME   = "0";
    public static final String K_SECTOR_6_EXIT_TIME   = "org.paternostro.elkron.sector.6.exit.time";
    public static final String D_SECTOR_6_EXIT_TIME   = "0";
    public static final String K_SECTOR_7_EXIT_TIME   = "org.paternostro.elkron.sector.7.exit.time";
    public static final String D_SECTOR_7_EXIT_TIME   = "0";
    public static final String K_SECTOR_8_EXIT_TIME   = "org.paternostro.elkron.sector.8.exit.time";
    public static final String D_SECTOR_8_EXIT_TIME   = "0";
    public static final String K_SECTOR_1_NAME  = "org.paternostro.elkron.sector.1.name";
    public static final String D_SECTOR_1_NAME  = "...                     ";
    public static final String K_SECTOR_2_NAME  = "org.paternostro.elkron.sector.2.name";
    public static final String D_SECTOR_2_NAME  = "...                     ";
    public static final String K_SECTOR_3_NAME  = "org.paternostro.elkron.sector.3.name";
    public static final String D_SECTOR_3_NAME  = "...                     ";
    public static final String K_SECTOR_4_NAME  = "org.paternostro.elkron.sector.4.name";
    public static final String D_SECTOR_4_NAME  = "...                     ";  
    public static final String K_SECTOR_5_NAME  = "org.paternostro.elkron.sector.5.name";
    public static final String D_SECTOR_5_NAME  = "...                     ";
    public static final String K_SECTOR_6_NAME  = "org.paternostro.elkron.sector.6.name";
    public static final String D_SECTOR_6_NAME  = "...                     ";
    public static final String K_SECTOR_7_NAME  = "org.paternostro.elkron.sector.7.name";
    public static final String D_SECTOR_7_NAME  = "...                     ";
    public static final String K_SECTOR_8_NAME  = "org.paternostro.elkron.sector.8.name";
    public static final String D_SECTOR_8_NAME  = "...                     ";
    public static final String K_SECTOR_1_TYPE  = "org.paternostro.elkron.sector.1.type";
    public static final String D_SECTOR_1_TYPE  = "STANDARD";
    public static final String K_SECTOR_2_TYPE  = "org.paternostro.elkron.sector.2.type";
    public static final String D_SECTOR_2_TYPE  = "STANDARD";
    public static final String K_SECTOR_3_TYPE  = "org.paternostro.elkron.sector.3.type";
    public static final String D_SECTOR_3_TYPE  = "STANDARD";
    public static final String K_SECTOR_4_TYPE  = "org.paternostro.elkron.sector.4.type";
    public static final String D_SECTOR_4_TYPE  = "STANDARD";
    public static final String K_SECTOR_5_TYPE  = "org.paternostro.elkron.sector.5.type";
    public static final String D_SECTOR_5_TYPE  = "STANDARD";
    public static final String K_SECTOR_6_TYPE  = "org.paternostro.elkron.sector.6.type";
    public static final String D_SECTOR_6_TYPE  = "STANDARD";
    public static final String K_SECTOR_7_TYPE  = "org.paternostro.elkron.sector.7.type";
    public static final String D_SECTOR_7_TYPE  = "STANDARD";
    public static final String K_SECTOR_8_TYPE  = "org.paternostro.elkron.sector.8.type";
    public static final String D_SECTOR_8_TYPE  = "STANDARD";

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
        
        switch (area) {
            case 1:
                key = K_AREA_1_SECTORS;
                defaultValue = D_AREA_1_SECTORS;
                break;
            case 2:
                key = K_AREA_2_SECTORS;
                defaultValue = D_AREA_2_SECTORS;
                break;
            case 3:
                key = K_AREA_3_SECTORS;
                defaultValue = D_AREA_3_SECTORS;
                break;
            case 4:
                key = K_AREA_4_SECTORS;
                defaultValue = D_AREA_4_SECTORS;
                break;
            default:
                logger.warn("Invalid area number: " + area + ", must be between 1 and 4");
                return 0;
        }

        return getIntProperty(key, defaultValue, "area " + area + " sectors");
    }

    public String getAreaName(int area) {
        String  key,
                defaultValue;
        
        switch (area) {
            case 1:
                key = K_AREA_1_NAME;
                defaultValue = D_AREA_1_NAME;
                break;
            case 2:
                key = K_AREA_2_NAME;
                defaultValue = D_AREA_2_NAME;
                break;
            case 3:
                key = K_AREA_3_NAME;
                defaultValue = D_AREA_3_NAME;
                break;
            case 4:
                key = K_AREA_4_NAME;
                defaultValue = D_AREA_4_NAME;
                break;
            default:
                logger.warn("Invalid area number: " + area + ", must be between 1 and 4");
                return "...";
        }

        return properties.getProperty(key, defaultValue);
    }

    public int getSectors() {
        return getIntProperty(K_SECTORS, D_SECTORS, "sectors");
    }

    public int getSectorEntryTime(int sector) {
        String  key,
                defaultValue;
        
        switch (sector) {
            case 1:
                key = K_SECTOR_1_ENTRY_TIME;
                defaultValue = D_SECTOR_1_ENTRY_TIME;
                break;
            case 2:
                key = K_SECTOR_2_ENTRY_TIME;
                defaultValue = D_SECTOR_2_ENTRY_TIME;
                break;
            case 3:
                key = K_SECTOR_3_ENTRY_TIME;
                defaultValue = D_SECTOR_3_ENTRY_TIME;
                break;
            case 4:
                key = K_SECTOR_4_ENTRY_TIME;
                defaultValue = D_SECTOR_4_ENTRY_TIME;
                break;
            case 5:
                key = K_SECTOR_5_ENTRY_TIME;
                defaultValue = D_SECTOR_5_ENTRY_TIME;
                break;
            case 6:
                key = K_SECTOR_6_ENTRY_TIME;
                defaultValue = D_SECTOR_6_ENTRY_TIME;
                break;
            case 7:
                key = K_SECTOR_7_ENTRY_TIME;
                defaultValue = D_SECTOR_7_ENTRY_TIME;
                break;
            case 8:
                key = K_SECTOR_8_ENTRY_TIME;
                defaultValue = D_SECTOR_8_ENTRY_TIME;
                break;    
            default:
                logger.warn("Invalid sector number: " + sector + ", must be between 1 and 8");
                return 0;
        }

        return getIntProperty(key, defaultValue, "sector " + sector + " entry time");
    }

    public int getSectorExitTime(int sector) {
        String  key,
                defaultValue;
        
        switch (sector) {
            case 1:
                key = K_SECTOR_1_EXIT_TIME;
                defaultValue = D_SECTOR_1_EXIT_TIME;
                break;
            case 2:
                key = K_SECTOR_2_EXIT_TIME;
                defaultValue = D_SECTOR_2_EXIT_TIME;
                break;
            case 3:
                key = K_SECTOR_3_EXIT_TIME;
                defaultValue = D_SECTOR_3_EXIT_TIME;
                break;
            case 4:
                key = K_SECTOR_4_EXIT_TIME;
                defaultValue = D_SECTOR_4_EXIT_TIME;
                break;
            case 5:
                key = K_SECTOR_5_EXIT_TIME;
                defaultValue = D_SECTOR_5_EXIT_TIME;
                break;
            case 6:
                key = K_SECTOR_6_EXIT_TIME;
                defaultValue = D_SECTOR_6_EXIT_TIME;
                break;
            case 7:
                key = K_SECTOR_7_EXIT_TIME;
                defaultValue = D_SECTOR_7_EXIT_TIME;
                break;
            case 8:
                key = K_SECTOR_8_EXIT_TIME;
                defaultValue = D_SECTOR_8_EXIT_TIME;
                break;    
            default:
                logger.warn("Invalid sector number: " + sector + ", must be between 1 and 8");
                return 0;
        }

        return getIntProperty(key, defaultValue, "sector " + sector + " exit time");
    }

    public String getSectorName(int sector) {
        String  key,
                defaultValue;
        
        switch (sector) {
            case 1:
                key = K_SECTOR_1_NAME;
                defaultValue = D_SECTOR_1_NAME;
                break;
            case 2:
                key = K_SECTOR_2_NAME;
                defaultValue = D_SECTOR_2_NAME;
                break;
            case 3:
                key = K_SECTOR_3_NAME;
                defaultValue = D_SECTOR_3_NAME;
                break;
            case 4:
                key = K_SECTOR_4_NAME;
                defaultValue = D_SECTOR_4_NAME;
                break;
            case 5:
                key = K_SECTOR_5_NAME;
                defaultValue = D_SECTOR_5_NAME;
                break;
            case 6:
                key = K_SECTOR_6_NAME;
                defaultValue = D_SECTOR_6_NAME;
                break;
            case 7:
                key = K_SECTOR_7_NAME;
                defaultValue = D_SECTOR_7_NAME;
                break;
            case 8:
                key = K_SECTOR_8_NAME;
                defaultValue = D_SECTOR_8_NAME;
                break;    
            default:
                logger.warn("Invalid sector number: " + sector + ", must be between 1 and 8");
                return "...";
        }

        return properties.getProperty(key, defaultValue);
    }

    public Partition.Type getSectorType(int sector) {
        String  key,
                defaultValue;
        
        switch (sector) {
            case 1:
                key = K_SECTOR_1_TYPE;
                defaultValue = D_SECTOR_1_TYPE;
                break;
            case 2:
                key = K_SECTOR_2_TYPE;
                defaultValue = D_SECTOR_2_TYPE;
                break;
            case 3:
                key = K_SECTOR_3_TYPE;
                defaultValue = D_SECTOR_3_TYPE;
                break;
            case 4:
                key = K_SECTOR_4_TYPE;
                defaultValue = D_SECTOR_4_TYPE;
                break;
            case 5:
                key = K_SECTOR_5_TYPE;
                defaultValue = D_SECTOR_5_TYPE;
                break;
            case 6:
                key = K_SECTOR_6_TYPE;
                defaultValue = D_SECTOR_6_TYPE;
                break;
            case 7:
                key = K_SECTOR_7_TYPE;
                defaultValue = D_SECTOR_7_TYPE;
                break;
            case 8:
                key = K_SECTOR_8_TYPE;
                defaultValue = D_SECTOR_8_TYPE;
                break;
            default:
                logger.warn("Invalid sector number: " + sector + ", must be between 1 and 8");
                return null;
        }

        return Partition.Type.valueOf(properties.getProperty(key, defaultValue));
    }
}
