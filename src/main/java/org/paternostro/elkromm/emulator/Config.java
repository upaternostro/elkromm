package org.paternostro.elkromm.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Output;
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

    public static final String K_INPUT_CONFIGURATION    = "org.paternostro.elkron.input.%d.configuration";
    public static final String D_INPUT_CONFIGURATION    = "NORMALLY_CLOSED_DOUBLE_BALANCED";

    public static final String K_INPUT_SPECIALIZATION   = "org.paternostro.elkron.input.%d.specialization";
    public static final String D_INPUT_SPECIALIZATION   = "IMMEDIATE";

    public static final String K_INPUT_SENSITIVITY  = "org.paternostro.elkron.input.%d.sensitivity";
    public static final String D_INPUT_SENSITIVITY  = "HIGH";

    public static final String K_INPUT_FLAGS    = "org.paternostro.elkron.input.%d.flags";
    public static final String D_INPUT_FLAGS    = "0";

    public static final String K_INPUT_VIDEO    = "org.paternostro.elkron.input.%d.video";
    public static final String D_INPUT_VIDEO    = "NONE";

    public static final String K_INPUT_PARTITIONS   = "org.paternostro.elkron.input.%d.partitions";
    public static final String D_INPUT_PARTITIONS   = "1";

    public static final String K_INPUT_NAME     = "org.paternostro.elkron.input.%d.name";
    public static final String D_INPUT_NAME     = "Input %d";

    public static final String K_INPUT_DELAY    = "org.paternostro.elkron.input.%d.delay";
    public static final String D_INPUT_DELAY    = "5_SECS";

    public static final String K_OUTPUT_TYPE    = "org.paternostro.elkron.output.%d.type";
    public static final String D_OUTPUT_TYPE    = "NORMALLY_LOW";

    public static final String K_OUTPUT_PARTITIONS    = "org.paternostro.elkron.output.%d.partitions";
    public static final String D_OUTPUT_PARTITIONS    = "1";

    public static final String K_OUTPUT_SPECIALIZATION    = "org.paternostro.elkron.output.%d.specialization";
    public static final String D_OUTPUT_SPECIALIZATION    = "OR_TC";

    public static final String K_OUTPUT_NAME    = "org.paternostro.elkron.output.%d.name";
    public static final String D_OUTPUT_NAME    = "Output %d";

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

    public Input.Configuration getInputConfiguration(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return null;
        }
        
        return Input.Configuration.valueOf("IC_" + properties.getProperty(String.format(K_INPUT_CONFIGURATION, input), D_INPUT_CONFIGURATION));
    }

    public Input.Specialization getInputSpecialization(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return null;
        }
        
        return Input.Specialization.valueOf("IS_" + properties.getProperty(String.format(K_INPUT_SPECIALIZATION, input), D_INPUT_SPECIALIZATION));
    }

    public Input.Sensitivity getInputSensitivity(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return null;
        }
        
        return Input.Sensitivity.valueOf("IS_" + properties.getProperty(String.format(K_INPUT_SENSITIVITY, input), D_INPUT_SENSITIVITY));
    }

    public byte getInputFlags(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_INPUT_FLAGS, input), D_INPUT_FLAGS, "inputs");
    }

    public Input.Video getInputVideo(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return null;
        }
        
        return Input.Video.valueOf("IV_" + properties.getProperty(String.format(K_INPUT_VIDEO, input), D_INPUT_VIDEO));
    }

    public byte getInputPartitions(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_INPUT_PARTITIONS, input), D_INPUT_PARTITIONS, "inputs");
    }

    public String getInputName(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_INPUT_NAME, input), String.format(D_INPUT_NAME, input));
    }

    public Input.Delay getInputDelay(int input) {
        if (input < 1 || input > ElkrommFacade.MAX_LOGICAL_INPUTS) {
            logger.warn("Invalid input number: " + input + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_INPUTS);
            return null;
        }
        
        return Input.Delay.valueOf("ID_" + properties.getProperty(String.format(K_INPUT_DELAY, input), D_INPUT_DELAY));
    }

    public Output.Type getOutputType(int output) {
        if (output < 1 || output > ElkrommFacade.MAX_LOGICAL_OUTPUTS) {
            logger.warn("Invalid input number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
            return null;
        }
        
        return Output.Type.valueOf("OT_" + properties.getProperty(String.format(K_OUTPUT_TYPE, output), D_OUTPUT_TYPE));
    }

    public byte getOutputPartitions(int output) {
        if (output < 1 || output > ElkrommFacade.MAX_LOGICAL_OUTPUTS) {
            logger.warn("Invalid input number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_OUTPUT_PARTITIONS, output), D_OUTPUT_PARTITIONS, "outputs");
    }

    public Output.Specialization getOutputSpecialization(int output) {
        if (output < 1 || output > ElkrommFacade.MAX_LOGICAL_OUTPUTS) {
            logger.warn("Invalid input number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
            return null;
        }
        
        return Output.Specialization.valueOf("OS_" + properties.getProperty(String.format(K_OUTPUT_SPECIALIZATION, output), D_OUTPUT_SPECIALIZATION));
    }

    public String getOutputName(int output) {
        if (output < 1 || output > ElkrommFacade.MAX_LOGICAL_OUTPUTS) {
            logger.warn("Invalid output number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_OUTPUT_NAME, output), String.format(D_OUTPUT_NAME, output));
    }
}
