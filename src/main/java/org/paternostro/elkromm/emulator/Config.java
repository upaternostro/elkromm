package org.paternostro.elkromm.emulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.DayClassCommands;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.Output;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.Partition;
import org.paternostro.elkromm.dto.PhoneNumber;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration of the emulator, read from the {@value #PROPERTIES_FILE_NAME} file found on the classpath.
 * <p>
 * Every property has a {@code K_*} constant with its key and a {@code D_*} constant with its default, used
 * when the property is missing. Keys that apply to many objects (inputs, users, keypads...) are
 * {@link String#format} patterns, filled with the number of the object.
 * <p>
 * Values are written as text:
 * <ul>
 * <li>numbers, bitmasks and raw bytes are written in decimal;</li>
 * <li>enumerations are written with the name of the constant <b>without its prefix</b>, in upper case. Each
 * enumeration of the library prefixes its constants with an abbreviation of its name (for example
 * {@code COT_PARTITIONS} in {@link Command.ObjectType}, {@code IC_NOT_USED} in {@link Input.Configuration}
 * or {@code P_ONE} in {@link ElkrommFacade.Partition}): in the properties file the prefix is dropped, so they
 * become {@code PARTITIONS}, {@code NOT_USED} and {@code ONE}. The documentation of each key lists the
 * enumeration it takes its values from.</li>
 * </ul>
 * <p>
 * An invalid value is not always handled in the same way: a name that does not belong to an enumeration makes
 * the getter throw an {@link IllegalArgumentException}, while an invalid number or hexadecimal string is
 * logged and replaced by the default.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Config {
    public static final Logger logger       = LoggerFactory.getLogger(Config.class);

    /** Name of the optional classpath properties file used to override components. */
    public static final String PROPERTIES_FILE_NAME = "elkromm-emulator.properties";
    /** Property key used to override the {@code Config} implementation class itself. */
    public static final String CLASS_NAME_PROPERTY  = "org.paternostro.elkromm.emulator.Config.class";
    /** Default {@code Config} implementation class name. */
    public static final String CLASS_NAME_DEFAULT   = "org.paternostro.elkromm.emulator.Config";

    /** The shared instance, created lazily by {@link #getInstance()}. */
    protected static volatile Config instance = null;

    /**
     * Returns the shared configuration, creating it on first use from the properties file
     * {@value #PROPERTIES_FILE_NAME} found on the classpath. The implementation class can be replaced
     * through the property {@value #CLASS_NAME_PROPERTY}.
     *
     * @return the shared {@code Config} instance
     */
    public static Config getInstance()
    {
        Config   retval = instance;

        if (retval == null) {
            synchronized (Config.class) {
                retval = instance;

                if (retval == null) {
                    Properties  p = loadProperties();
                    String      className = p.getProperty(CLASS_NAME_PROPERTY, CLASS_NAME_DEFAULT);

                    try {
                        retval = Class.forName(className, false, Config.class.getClassLoader())
                                        .asSubclass(Config.class)
                                        .getDeclaredConstructor()
                                        .newInstance();
                    } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                        logger.warn("Cannot instantiate class " + className + ", using default class", e);
                        retval = new Config();
                    }

                    /*
                     * Configure the new instance before publishing it in 'instance': other threads calling
                     * getInstance() must never see a Config that does not have its properties yet
                     */
                    retval.setProperties(p);
                    instance = retval;
                }
            }
        }

        return retval;
    }

    /**
     * Loads {@value #PROPERTIES_FILE_NAME} from the classpath.
     *
     * @return the loaded properties, empty if the file is missing or cannot be read
     */
    protected static Properties loadProperties() {
        Properties  p = new Properties();

        try (InputStream propertiesStream = Config.class.getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (propertiesStream != null) {
                p.load(propertiesStream);
            }
        } catch (IOException e) {
            logger.warn("Error reading " + PROPERTIES_FILE_NAME + ", using defaults", e);
        }

        return p;
    }

    /** Property key: TCP port the emulator listens on. */
    public static final String K_PORT           = "org.paternostro.elkromm.emulator.port";
    /** Default value of {@link #K_PORT}. */
    public static final String D_PORT           = "8030";

    /** Property key: plant code the emulator expects at login. */
    public static final String K_PLANT_CODE     = "org.paternostro.elkromm.emulator.plant.code";
    /** Default value of {@link #K_PLANT_CODE}. */
    public static final String D_PLANT_CODE     = "55555555";
    /** Property key: technician PIN the emulator expects at login. */
    public static final String K_TECHNICAL_CODE = "org.paternostro.elkromm.emulator.technical.code";
    /** Default value of {@link #K_TECHNICAL_CODE}. */
    public static final String D_TECHNICAL_CODE = "000000";

    /** Property key: number of keypads connected to the emulated panel. */
    public static final String K_KEYBOARDS      = "org.paternostro.elkromm.emulator.keyboards";
    /** Default value of {@link #K_KEYBOARDS}. */
    public static final String D_KEYBOARDS      = "0";
    /** Property key: number of readers connected to the emulated panel. */
    public static final String K_READERS        = "org.paternostro.elkromm.emulator.readers";
    /** Default value of {@link #K_READERS}. */
    public static final String D_READERS        = "0";

    /** Property key: number of expansion boards connected to the emulated panel (not counting the one embedded in the panel). */
    public static final String K_EXPANSIONS     = "org.paternostro.elkromm.emulator.expansions";
    /** Default value of {@link #K_EXPANSIONS}. */
    public static final String D_EXPANSIONS     = "0";

    /** Property key: number of areas of the emulated panel. */
    public static final String K_AREAS          = "org.paternostro.elkromm.emulator.areas";
    /** Default value of {@link #K_AREAS}. */
    public static final String D_AREAS          = "0";

    /** Property key: partitions of an area. This is a {@link String#format} pattern, filled with the area number (1-based). */
    public static final String K_AREA_PARTITIONS    = "org.paternostro.elkromm.emulator.area.%d.partitions";
    /** Default values of {@link #K_AREA_PARTITIONS}, indexed by the area number minus one. */
    public static final String[] D_AREA_PARTITIONS  = {"1", "0", "0", "0"};
    /** Property key: name of an area. This is a {@link String#format} pattern, filled with the area number (1-based). */
    public static final String K_AREA_NAME      = "org.paternostro.elkromm.emulator.area.%d.name";
    /** Default values of {@link #K_AREA_NAME}, indexed by the area number minus one. */
    public static final String[] D_AREA_NAME    = {"...", ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME};
    
    /** Property key: number of partitions of the emulated panel. */
    public static final String K_PARTITIONS     = "org.paternostro.elkromm.emulator.partitions";
    /** Default value of {@link #K_PARTITIONS}. */
    public static final String D_PARTITIONS     = "1";

    /** Property key: entry time of a partition, see {@link Partition.EntryExitDelay}. This is a {@link String#format} pattern, filled with the partition number (1-based). */
    public static final String K_PARTITION_ENTRY_TIME   = "org.paternostro.elkromm.emulator.partition.%d.entry.time";
    /** Default value of {@link #K_PARTITION_ENTRY_TIME}. */
    public static final String D_PARTITION_ENTRY_TIME   = "DISABLED";
    /** Property key: exit time of a partition, see {@link Partition.EntryExitDelay}. This is a {@link String#format} pattern, filled with the partition number (1-based). */
    public static final String K_PARTITION_EXIT_TIME    = "org.paternostro.elkromm.emulator.partition.%d.exit.time";
    /** Default value of {@link #K_PARTITION_EXIT_TIME}. */
    public static final String D_PARTITION_EXIT_TIME    = "DISABLED";
    /** Property key: name of a partition. This is a {@link String#format} pattern, filled with the partition number (1-based). */
    public static final String K_PARTITION_NAME = "org.paternostro.elkromm.emulator.partition.%d.name";
    /** Default value of {@link #K_PARTITION_NAME}. */
    public static final String D_PARTITION_NAME = ElkrommFacade.DEFAULT_NAME;
    /** Property key: flag that indicates the presence of a vocal name of a partition. This is a {@link String#format} pattern, filled with the partition number (1-based). */
    public static final String K_PARTITION_VOCAL_NAME   = "org.paternostro.elkromm.emulator.partition.%d.vocalName";
    /** Default value of {@link #K_PARTITION_VOCAL_NAME}. */
    public static final String D_PARTITION_VOCAL_NAME   = "false";
    /** Property key: type of a partition, see {@link Partition.Type}. This is a {@link String#format} pattern, filled with the partition number (1-based). */
    public static final String K_PARTITION_TYPE = "org.paternostro.elkromm.emulator.partition.%d.type";
    /** Default value of {@link #K_PARTITION_TYPE}. */
    public static final String D_PARTITION_TYPE = "STANDARD";

    /** Property key: name of a user. This is a {@link String#format} pattern, filled with the user number (1-based). */
    public static final String K_USER_NAME      = "org.paternostro.elkromm.emulator.user.%d.name";
    /** Default values of {@link #K_USER_NAME}, indexed by the user number minus one. */
    public static final String[] D_USER_NAME    = { "TECNICO                 ", 
                                                    "MASTER                  ",
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, 
                                                    ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME };
    /** Property key: enablings of a user, see {@link Credential.Enabling}. This is a {@link String#format} pattern, filled with the user number (1-based). */
    public static final String K_USER_ENABLINGS    = "org.paternostro.elkromm.emulator.user.%d.enablings";
    /** Default values of {@link #K_USER_ENABLINGS}, indexed by the user number minus one. */
    public static final String[] D_USER_ENABLINGS  = { Byte.toString(User.Enabling.DISABLED.getValue()), 
                                                        Byte.toString((byte)(User.Enabling.ALWAYS_ENABLED.getValue() | User.Enabling.ENABLED.getValue())), 
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()),
                                                        Byte.toString(User.Enabling.DISABLED.getValue()), Byte.toString(User.Enabling.DISABLED.getValue()) };
    /** Property key: partitions of a user. This is a {@link String#format} pattern, filled with the user number (1-based). */
    public static final String K_USER_PARTITIONS    = "org.paternostro.elkromm.emulator.user.%d.partitions";
    /** Default values of {@link #K_USER_PARTITIONS}, indexed by the user number minus one. */
    public static final String[] D_USER_PARTITIONS  = { "255", "255", 
                                                        "1", "1", "1", "1", "1", "1", "1", "1",
                                                        "1", "1", "1", "1", "1", "1", "1", "1",
                                                        "1", "1", "1", "1", "1", "1", "1", "1",
                                                        "1", "1", "1", "1", "1", "1" };

    /** Property key: name of a key. This is a {@link String#format} pattern, filled with the key number (1-based). */
    public static final String K_KEY_NAME   = "org.paternostro.elkromm.emulator.key.%d.name";
    /** Default value of {@link #K_KEY_NAME}. */
    public static final String D_KEY_NAME   = ElkrommFacade.DEFAULT_NAME;
    /** Property key: enablings of a key, see {@link Credential.Enabling}. This is a {@link String#format} pattern, filled with the key number (1-based). */
    public static final String K_KEY_ENABLINGS  = "org.paternostro.elkromm.emulator.key.%d.enablings";
    /** Default value of {@link #K_KEY_ENABLINGS}. */
    public static final String D_KEY_ENABLINGS  = Byte.toString(Credential.Enabling.DISABLED.getValue());
    /** Property key: specialization of a key, see {@link Key.Specialization}. This is a {@link String#format} pattern, filled with the key number (1-based). */
    public static final String K_KEY_SPECIALIZATION = "org.paternostro.elkromm.emulator.key.%d.specialization";
    /** Default value of {@link #K_KEY_SPECIALIZATION}. */
    public static final String D_KEY_SPECIALIZATION = "NONE";
    /** Property key: partitions of a key. This is a {@link String#format} pattern, filled with the key number (1-based). */
    public static final String K_KEY_PARTITIONS = "org.paternostro.elkromm.emulator.key.%d.partitions";
    /** Default value of {@link #K_KEY_PARTITIONS}. */
    public static final String D_KEY_PARTITIONS = "1";

    /** Property key: name of an expansion. This is a {@link String#format} pattern, filled with the expansion number (0 is the unit embedded in the panel). */
    public static final String K_EXPANSION_NAME             = "org.paternostro.elkromm.emulator.expansion.%d.name";
    /** Default value of {@link #K_EXPANSION_NAME} for the expansion embedded in the panel (expansion 0). */
    public static final String D_EMBEDDED_EXPANSION_NAME    = "UC        ";
    /** Default value of {@link #K_EXPANSION_NAME} for the expansion boards (expansion 1 and following). It is a {@link String#format} pattern filled with the expansion number. */
    public static final String D_EXPANSION_NAME             = "EP %02d                   ";
    /** Property key: version of an expansion. This is a {@link String#format} pattern, filled with the expansion number (0 is the unit embedded in the panel). */
    public static final String K_EXPANSION_VERSION          = "org.paternostro.elkromm.emulator.expansion.%d.version";
    /** Default value of {@link #K_EXPANSION_VERSION} for the expansion embedded in the panel (expansion 0). */
    public static final String D_EMBEDDED_EXPANSION_VERSION = "0301";
    /** Default value of {@link #K_EXPANSION_VERSION} for the expansion boards (expansion 1 and following). */
    public static final String D_EXPANSION_VERSION          = "0200";

    /** Property key: configuration of an input, see {@link Input.Configuration}. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_CONFIGURATION    = "org.paternostro.elkromm.emulator.input.%d.configuration";
    /** Default value of {@link #K_INPUT_CONFIGURATION}. */
    public static final String D_INPUT_CONFIGURATION    = "NOT_USED";
    /** Property key: specialization of an input, see {@link Input.Specialization}. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_SPECIALIZATION   = "org.paternostro.elkromm.emulator.input.%d.specialization";
    /** Default value of {@link #K_INPUT_SPECIALIZATION}. */
    public static final String D_INPUT_SPECIALIZATION   = "IMMEDIATE";
    /** Property key: sensitivity of an input, see {@link Input.Sensitivity}. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_SENSITIVITY  = "org.paternostro.elkromm.emulator.input.%d.sensitivity";
    /** Default value of {@link #K_INPUT_SENSITIVITY}. */
    public static final String D_INPUT_SENSITIVITY  = "HIGH";
    /** Property key: flags of an input, see {@link Input.Flags}. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_FLAGS    = "org.paternostro.elkromm.emulator.input.%d.flags";
    /** Default value of {@link #K_INPUT_FLAGS}. */
    public static final String D_INPUT_FLAGS    = "0";
    /** Property key: video of an input, see {@link Input.Video}. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_VIDEO    = "org.paternostro.elkromm.emulator.input.%d.video";
    /** Default value of {@link #K_INPUT_VIDEO}. */
    public static final String D_INPUT_VIDEO    = "NONE";
    /** Property key: partitions of an input. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_PARTITIONS   = "org.paternostro.elkromm.emulator.input.%d.partitions";
    /** Default value of {@link #K_INPUT_PARTITIONS}. */
    public static final String D_INPUT_PARTITIONS   = "1";
    /** Property key: name of an input. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_NAME     = "org.paternostro.elkromm.emulator.input.%d.name";
    /** Default value of {@link #K_INPUT_NAME}. It is a {@link String#format} pattern. */
    public static final String D_INPUT_NAME     = "Input %d";
    /** Property key: delay of an input, see {@link Input.Delay}. This is a {@link String#format} pattern, filled with the logical input number. */
    public static final String K_INPUT_DELAY    = "org.paternostro.elkromm.emulator.input.%d.delay";
    /** Default value of {@link #K_INPUT_DELAY}. */
    public static final String D_INPUT_DELAY    = "5_SECS";

    /** Property key: type of an output, see {@link Output.Type}. This is a {@link String#format} pattern, filled with the logical output number. */
    public static final String K_OUTPUT_TYPE    = "org.paternostro.elkromm.emulator.output.%d.type";
    /** Default value of {@link #K_OUTPUT_TYPE}. */
    public static final String D_OUTPUT_TYPE    = "NORMALLY_LOW";
    /** Property key: partitions of an output. This is a {@link String#format} pattern, filled with the logical output number. */
    public static final String K_OUTPUT_PARTITIONS    = "org.paternostro.elkromm.emulator.output.%d.partitions";
    /** Default value of {@link #K_OUTPUT_PARTITIONS}. */
    public static final String D_OUTPUT_PARTITIONS    = "1";
    /** Property key: specialization of an output, see {@link Output.Specialization}. This is a {@link String#format} pattern, filled with the logical output number. */
    public static final String K_OUTPUT_SPECIALIZATION    = "org.paternostro.elkromm.emulator.output.%d.specialization";
    /** Default value of {@link #K_OUTPUT_SPECIALIZATION}. */
    public static final String D_OUTPUT_SPECIALIZATION    = "OR_TC";
    /** Property key: name of an output. This is a {@link String#format} pattern, filled with the logical output number. */
    public static final String K_OUTPUT_NAME    = "org.paternostro.elkromm.emulator.output.%d.name";
    /** Default value of {@link #K_OUTPUT_NAME}. It is a {@link String#format} pattern. */
    public static final String D_OUTPUT_NAME    = "Output %d";

    /** Property key: name of a keypad. This is a {@link String#format} pattern, filled with the keypad number (1-based). */
    public static final String K_KEYPAD_NAME    = "org.paternostro.elkromm.emulator.keypad.%d.name";
    /** Default value of {@link #K_KEYPAD_NAME}. It is a {@link String#format} pattern. */
    public static final String D_KEYPAD_NAME    = "KP %02d                   ";
    /** Property key: version of a keypad. This is a {@link String#format} pattern, filled with the keypad number (1-based). */
    public static final String K_KEYPAD_VERSION = "org.paternostro.elkromm.emulator.keypad.%d.version";
    /** Default value of {@link #K_KEYPAD_VERSION}. */
    public static final String D_KEYPAD_VERSION = "0200";
    /** Property key: enablings of a keypad, see {@link Keyboard.Enablings}. This is a {@link String#format} pattern, filled with the keypad number (1-based). */
    public static final String K_KEYPAD_ENABLINGS  = "org.paternostro.elkromm.emulator.keypad.%d.enablings";
    /** Default value of {@link #K_KEYPAD_ENABLINGS}. */
    public static final String D_KEYPAD_ENABLINGS  = Byte.toString((byte)(Keyboard.Enablings.KE_ENTRY.getValue() | Keyboard.Enablings.KE_EXIT.getValue()));
    /** Property key: partitions of a keypad. This is a {@link String#format} pattern, filled with the keypad number (1-based). */
    public static final String K_KEYPAD_PARTITIONS = "org.paternostro.elkromm.emulator.keypad.%d.partitions";
    /** Default value of {@link #K_KEYPAD_PARTITIONS}. */
    public static final String D_KEYPAD_PARTITIONS = "1";
    /** Property key: audio features of a keypad, see {@link Keyboard.AudioFeatures}. This is a {@link String#format} pattern, filled with the keypad number (1-based). */
    public static final String K_KEYPAD_AUDIO_FEATURES = "org.paternostro.elkromm.emulator.keypad.%d.audioFeatures";
    /** Default value of {@link #K_KEYPAD_AUDIO_FEATURES}. */
    public static final String D_KEYPAD_AUDIO_FEATURES = Byte.toString(Keyboard.AudioFeatures.KA_NONE.getValue());

    /** Which of the two inputs hosted by a keypad or a reader. */
    public enum InputOrdinal {
        /** The first input. */
        FIRST,
        /** The second input. */
        SECOND
    };

    /** Property key: configuration of an input of a keypad, see {@link Input.Configuration}. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_CONFIGURATION    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.configuration";
    /** Default value of {@link #K_KEYPAD_INPUT_CONFIGURATION}. */
    public static final String D_KEYPAD_INPUT_CONFIGURATION    = "NOT_USED";
    /** Property key: specialization of an input of a keypad, see {@link Input.Specialization}. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_SPECIALIZATION   = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.specialization";
    /** Default value of {@link #K_KEYPAD_INPUT_SPECIALIZATION}. */
    public static final String D_KEYPAD_INPUT_SPECIALIZATION   = "IMMEDIATE";
    /** Property key: sensitivity of an input of a keypad, see {@link Input.Sensitivity}. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_SENSITIVITY  = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.sensitivity";
    /** Default value of {@link #K_KEYPAD_INPUT_SENSITIVITY}. */
    public static final String D_KEYPAD_INPUT_SENSITIVITY  = "HIGH";
    /** Property key: flags of an input of a keypad, see {@link Input.Flags}. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_FLAGS    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.flags";
    /** Default value of {@link #K_KEYPAD_INPUT_FLAGS}. */
    public static final String D_KEYPAD_INPUT_FLAGS    = "0";
    /** Property key: video of an input of a keypad, see {@link Input.Video}. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_VIDEO    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.video";
    /** Default value of {@link #K_KEYPAD_INPUT_VIDEO}. */
    public static final String D_KEYPAD_INPUT_VIDEO    = "NONE";
    /** Property key: partitions of an input of a keypad. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_PARTITIONS   = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.partitions";
    /** Default value of {@link #K_KEYPAD_INPUT_PARTITIONS}. */
    public static final String D_KEYPAD_INPUT_PARTITIONS   = "1";
    /** Property key: name of an input of a keypad. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_NAME     = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.name";
    /** Default value of {@link #K_KEYPAD_INPUT_NAME}. It is a {@link String#format} pattern. */
    public static final String D_KEYPAD_INPUT_NAME     = "Input %d";
    /** Property key: delay of an input of a keypad, see {@link Input.Delay}. This is a {@link String#format} pattern, filled with the keypad number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_KEYPAD_INPUT_DELAY    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.delay";
    /** Default value of {@link #K_KEYPAD_INPUT_DELAY}. */
    public static final String D_KEYPAD_INPUT_DELAY    = "5_SECS";

    /** Property key: name of a reader. This is a {@link String#format} pattern, filled with the reader number (1-based). */
    public static final String K_READER_NAME    = "org.paternostro.elkromm.emulator.reader.%d.name";
    /** Default value of {@link #K_READER_NAME}. It is a {@link String#format} pattern. */
    public static final String D_READER_NAME    = "DK %02d                   ";
    /** Property key: enablings of a reader, see {@link Reader.Enablings}. This is a {@link String#format} pattern, filled with the reader number (1-based). */
    public static final String K_READER_ENABLINGS  = "org.paternostro.elkromm.emulator.reader.%d.enablings";
    /** Default value of {@link #K_READER_ENABLINGS}. */
    public static final String D_READER_ENABLINGS  = Byte.toString(Reader.Enablings.RE_NONE.getValue());
    /** Property key: partition associated with an LED of a reader, see {@link ElkrommFacade.Partition}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the LED number. */
    public static final String K_READER_LED = "org.paternostro.elkromm.emulator.reader.%d.led%d";
    /** Default value of {@link #K_READER_LED}. */
    public static final String D_READER_LED = "NONE";

    /** Property key: configuration of an input of a reader, see {@link Input.Configuration}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_CONFIGURATION    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.configuration";
    /** Default value of {@link #K_READER_INPUT_CONFIGURATION}. */
    public static final String D_READER_INPUT_CONFIGURATION    = "NOT_USED";
    /** Property key: specialization of an input of a reader, see {@link Input.Specialization}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_SPECIALIZATION   = "org.paternostro.elkromm.emulator.reader.%d.%sInput.specialization";
    /** Default value of {@link #K_READER_INPUT_SPECIALIZATION}. */
    public static final String D_READER_INPUT_SPECIALIZATION   = "IMMEDIATE";
    /** Property key: sensitivity of an input of a reader, see {@link Input.Sensitivity}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_SENSITIVITY  = "org.paternostro.elkromm.emulator.reader.%d.%sInput.sensitivity";
    /** Default value of {@link #K_READER_INPUT_SENSITIVITY}. */
    public static final String D_READER_INPUT_SENSITIVITY  = "HIGH";
    /** Property key: flags of an input of a reader, see {@link Input.Flags}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_FLAGS    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.flags";
    /** Default value of {@link #K_READER_INPUT_FLAGS}. */
    public static final String D_READER_INPUT_FLAGS    = "0";
    /** Property key: video of an input of a reader, see {@link Input.Video}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_VIDEO    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.video";
    /** Default value of {@link #K_READER_INPUT_VIDEO}. */
    public static final String D_READER_INPUT_VIDEO    = "NONE";
    /** Property key: partitions of an input of a reader. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_PARTITIONS   = "org.paternostro.elkromm.emulator.reader.%d.%sInput.partitions";
    /** Default value of {@link #K_READER_INPUT_PARTITIONS}. */
    public static final String D_READER_INPUT_PARTITIONS   = "1";
    /** Property key: name of an input of a reader. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_NAME     = "org.paternostro.elkromm.emulator.reader.%d.%sInput.name";
    /** Default value of {@link #K_READER_INPUT_NAME}. It is a {@link String#format} pattern. */
    public static final String D_READER_INPUT_NAME     = "Input %d";
    /** Property key: delay of an input of a reader, see {@link Input.Delay}. This is a {@link String#format} pattern, filled with the reader number (1-based), then the input ordinal ({@code first} or {@code second}, the lowercase name of {@link InputOrdinal}). */
    public static final String K_READER_INPUT_DELAY    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.delay";
    /** Default value of {@link #K_READER_INPUT_DELAY}. */
    public static final String D_READER_INPUT_DELAY    = "5_SECS";

    /** Property key: burglar time (general parameters and enablings of the panel), see {@link ParametersEnablings.Time}. */
    public static final String K_PARAMETERS_ENABLINGS_BURGLAR_TIME  = "org.paternostro.elkromm.emulator.parametersEnablings.burglarTime";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_BURGLAR_TIME}. */
    public static final String D_PARAMETERS_ENABLINGS_BURGLAR_TIME  = "180_SECS";
    /** Property key: emergency time (general parameters and enablings of the panel), see {@link ParametersEnablings.Time}. */
    public static final String K_PARAMETERS_ENABLINGS_EMERGENCY_TIME    = "org.paternostro.elkromm.emulator.parametersEnablings.emergencyTime";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_EMERGENCY_TIME}. */
    public static final String D_PARAMETERS_ENABLINGS_EMERGENCY_TIME    = "180_SECS";
    /** Property key: pre-alarm time (general parameters and enablings of the panel), see {@link ParametersEnablings.Time}. */
    public static final String K_PARAMETERS_ENABLINGS_PRE_ALARM_TIME    = "org.paternostro.elkromm.emulator.parametersEnablings.preAlarmTime";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_PRE_ALARM_TIME}. */
    public static final String D_PARAMETERS_ENABLINGS_PRE_ALARM_TIME    = "180_SECS";
    /** Property key: alarm count (general parameters and enablings of the panel), see {@link ParametersEnablings.AlarmCount}. */
    public static final String K_PARAMETERS_ENABLINGS_ALARM_COUNT   = "org.paternostro.elkromm.emulator.parametersEnablings.alarmCount";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_ALARM_COUNT}. */
    public static final String D_PARAMETERS_ENABLINGS_ALARM_COUNT   = "NO_COUNT";
    /** Property key: power lack (general parameters and enablings of the panel), see {@link ParametersEnablings.PowerLack}. */
    public static final String K_PARAMETERS_ENABLINGS_POWER_LACK    = "org.paternostro.elkromm.emulator.parametersEnablings.powerLack";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_POWER_LACK}. */
    public static final String D_PARAMETERS_ENABLINGS_POWER_LACK    = "1_HOUR";
    /** Property key: play (general parameters and enablings of the panel), see {@link ParametersEnablings.Play}. */
    public static final String K_PARAMETERS_ENABLINGS_PLAY  = "org.paternostro.elkromm.emulator.parametersEnablings.play";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_PLAY}. */
    public static final String D_PARAMETERS_ENABLINGS_PLAY  = "0";
    /** Property key: help (general parameters and enablings of the panel), see {@link ParametersEnablings.Help}. */
    public static final String K_PARAMETERS_ENABLINGS_HELP  = "org.paternostro.elkromm.emulator.parametersEnablings.help";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_HELP}. */
    public static final String D_PARAMETERS_ENABLINGS_HELP  = "0";
    /** Property key: LAN (general parameters and enablings of the panel), see {@link ParametersEnablings.Enabling}. */
    public static final String K_PARAMETERS_ENABLINGS_LAN   = "org.paternostro.elkromm.emulator.parametersEnablings.lan";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_LAN}. */
    public static final String D_PARAMETERS_ENABLINGS_LAN   = "DISABLE";
    /** Property key: time programmer (general parameters and enablings of the panel), see {@link ParametersEnablings.Enabling}. */
    public static final String K_PARAMETERS_ENABLINGS_TIME_PROGRAMMER   = "org.paternostro.elkromm.emulator.parametersEnablings.timeProgrammer";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_TIME_PROGRAMMER}. */
    public static final String D_PARAMETERS_ENABLINGS_TIME_PROGRAMMER   = "DISABLE";
    /** Property key: notice (general parameters and enablings of the panel), see {@link ParametersEnablings.Notice}. */
    public static final String K_PARAMETERS_ENABLINGS_NOTICE    = "org.paternostro.elkromm.emulator.parametersEnablings.notice";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_NOTICE}. */
    public static final String D_PARAMETERS_ENABLINGS_NOTICE    = "NO_NOTICE";
    /** Property key: DST (general parameters and enablings of the panel), see {@link ParametersEnablings.DST}. */
    public static final String K_PARAMETERS_ENABLINGS_DST   = "org.paternostro.elkromm.emulator.parametersEnablings.dst";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_DST}. */
    public static final String D_PARAMETERS_ENABLINGS_DST   = "0";
    // FIXME: on&off DST da scambiare?
    /** Property key: DST on (general parameters and enablings of the panel), see {@link ParametersEnablings.Month}. */
    public static final String K_PARAMETERS_ENABLINGS_DST_ON    = "org.paternostro.elkromm.emulator.parametersEnablings.dst.on";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_DST_ON}. */
    public static final String D_PARAMETERS_ENABLINGS_DST_ON    = "OCTOBER";
    /** Property key: DST off (general parameters and enablings of the panel), see {@link ParametersEnablings.Month}. */
    public static final String K_PARAMETERS_ENABLINGS_DST_OFF   = "org.paternostro.elkromm.emulator.parametersEnablings.dst.off";
    /** Default value of {@link #K_PARAMETERS_ENABLINGS_DST_OFF}. */
    public static final String D_PARAMETERS_ENABLINGS_DST_OFF   = "MARCH";

    /** Property key: action of a command of the time programmer, see {@link Command.Action}. This is a {@link String#format} pattern, filled with the day class in camel case (e.g. {@code workingDay}), then the command number (1-based). */
    public static final String K_TIME_PROGRAMMER_COMMAND_ACTION = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.action";
    /** Default value of {@link #K_TIME_PROGRAMMER_COMMAND_ACTION}. */
    public static final String D_TIME_PROGRAMMER_COMMAND_ACTION = "NONE";
    /** Property key: object of a command of the time programmer. This is a {@link String#format} pattern, filled with the day class in camel case (e.g. {@code workingDay}), then the command number (1-based). */
    public static final String K_TIME_PROGRAMMER_COMMAND_OBJECT = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.object";
    /** Default value of {@link #K_TIME_PROGRAMMER_COMMAND_OBJECT}. */
    public static final String D_TIME_PROGRAMMER_COMMAND_OBJECT = "0";
    /** Property key: object type of a command of the time programmer, see {@link Command.ObjectType}. This is a {@link String#format} pattern, filled with the day class in camel case (e.g. {@code workingDay}), then the command number (1-based). */
    public static final String K_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE    = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.objectType";
    /** Default value of {@link #K_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE}. */
    public static final String D_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE    = "NONE";
    /** Property key: hour of a command of the time programmer. This is a {@link String#format} pattern, filled with the day class in camel case (e.g. {@code workingDay}), then the command number (1-based). */
    public static final String K_TIME_PROGRAMMER_COMMAND_HOUR   = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.hour";
    /** Default value of {@link #K_TIME_PROGRAMMER_COMMAND_HOUR}. */
    public static final String D_TIME_PROGRAMMER_COMMAND_HOUR   = "0";
    /** Property key: minute of a command of the time programmer. This is a {@link String#format} pattern, filled with the day class in camel case (e.g. {@code workingDay}), then the command number (1-based). */
    public static final String K_TIME_PROGRAMMER_COMMAND_MINUTE = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.minute";
    /** Default value of {@link #K_TIME_PROGRAMMER_COMMAND_MINUTE}. */
    public static final String D_TIME_PROGRAMMER_COMMAND_MINUTE = "0";

    /** Days of the week, keys of the emulator time programmer day class configuration. */
    public enum Weekday {
        /** Monday. */
        MONDAY,
        /** Tuesday. */
        TUESDAY,
        /** Wednesday. */
        WEDNESDAY,
        /** Thursday. */
        THURSDAY,
        /** Friday. */
        FRIDAY,
        /** Saturday. */
        SATURDAY,
        /** Sunday. */
        SUNDAY
    };

    /** Property key: day class of a weekday, see {@link DayClassCommands.DayClass}. This is a {@link String#format} pattern, filled with the weekday, lowercase (the name of a {@link Weekday}). */
    public static final String K_TIME_PROGRAMMER_DAY_CLASSES    = "org.paternostro.elkromm.emulator.timeProgrammer.dayClasses.%s";
    /** Default values of {@link #K_TIME_PROGRAMMER_DAY_CLASSES}, indexed by the weekday ordinal (Monday is 0). */
    public static final String[] D_TIME_PROGRAMMER_DAY_CLASSES  = {"WORKING_DAY", "WORKING_DAY", "WORKING_DAY", "WORKING_DAY", "WORKING_DAY", "PRE_HOLIDAY", "HOLIDAY"};

    /** Property key: call delay (phone parameters), see {@link PhoneParameters.Enabling}. */
    public static final String K_PHONE_PARAMETERS_CALL_DELAY    = "org.paternostro.elkromm.emulator.phoneParameters.callDelay";
    /** Default value of {@link #K_PHONE_PARAMETERS_CALL_DELAY}. */
    public static final String D_PHONE_PARAMETERS_CALL_DELAY    = "DISABLED";
    /** Property key: return call (phone parameters), see {@link PhoneParameters.ReturnCall}. */
    public static final String K_PHONE_PARAMETERS_RETURN_CALL   = "org.paternostro.elkromm.emulator.phoneParameters.returnCall";
    /** Default value of {@link #K_PHONE_PARAMETERS_RETURN_CALL}. */
    public static final String D_PHONE_PARAMETERS_RETURN_CALL   = "DISABLED";
    /** Property key: remote surveillance (phone parameters), see {@link PhoneParameters.Enabling}. */
    public static final String K_PHONE_PARAMETERS_REMOTE_SURVEILLANCE   = "org.paternostro.elkromm.emulator.phoneParameters.remoteSurveillance";
    /** Default value of {@link #K_PHONE_PARAMETERS_REMOTE_SURVEILLANCE}. */
    public static final String D_PHONE_PARAMETERS_REMOTE_SURVEILLANCE   = "DISABLED";
    /** Property key: voice message sending mode (phone parameters), see {@link PhoneParameters.VoiceMessagesSendingMode}. */
    public static final String K_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE    = "org.paternostro.elkromm.emulator.phoneParameters.voiceMessagesSendingMode";
    /** Default value of {@link #K_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE}. */
    public static final String D_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE    = "NONE";
    /** Property key: cyclic test call frequency (phone parameters), see {@link PhoneParameters.CyclicTestCallFrequency}. */
    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY    = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallFrequency";
    /** Default value of {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY}. */
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY    = "DISABLE";
    /** Property key: cyclic test call phone number (phone parameters). */
    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallPhoneNumber";
    /** Default value of {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER}. */
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER = "1";
    /** Property key: cyclic test call hour (phone parameters). */
    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallHour";
    /** Default value of {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR}. */
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR = "0";
    /** Property key: cyclic test call minute (phone parameters). */
    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE   = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallMinute";
    /** Default value of {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE}. */
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE   = "0";
    /** Property key: cyclic test call interval (phone parameters), see {@link PhoneParameters.CyclicTestCallInterval}. */
    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallInterval";
    /** Default value of {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL}. */
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL = "1_HOUR";

    /** Property key: enable PSTN (PSTN/GSM module), see {@link PSTNGSM.Enabling}. */
    public static final String K_PSTNGSM_ENABLE_PSTN    = "org.paternostro.elkromm.emulator.PSTNGSM.enablePSTN";
    /** Default value of {@link #K_PSTNGSM_ENABLE_PSTN}. */
    public static final String D_PSTNGSM_ENABLE_PSTN    = "DISABLED";
    /** Property key: country (PSTN/GSM module), see {@link PSTNGSM.Country}. */
    public static final String K_PSTNGSM_COUNTRY    = "org.paternostro.elkromm.emulator.PSTNGSM.country";
    /** Default value of {@link #K_PSTNGSM_COUNTRY}. */
    public static final String D_PSTNGSM_COUNTRY    = "ITALY";
    /** Property key: PABX local access digit (PSTN/GSM module), see {@link PSTNGSM.PABXLocalAccessDigit}. */
    public static final String K_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT    = "org.paternostro.elkromm.emulator.PSTNGSM.PABXLocalAccessDigit";
    /** Default value of {@link #K_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT}. */
    public static final String D_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT    = "DISABLE";
    /** Property key: tone control (PSTN/GSM module), see {@link PSTNGSM.Enabling}. */
    public static final String K_PSTNGSM_TONE_CONTROL   = "org.paternostro.elkromm.emulator.PSTNGSM.toneControl";
    /** Default value of {@link #K_PSTNGSM_TONE_CONTROL}. */
    public static final String D_PSTNGSM_TONE_CONTROL   = "DISABLED";
    /** Property key: answer control (PSTN/GSM module), see {@link PSTNGSM.Enabling}. */
    public static final String K_PSTNGSM_ANSWER_CONTROL = "org.paternostro.elkromm.emulator.PSTNGSM.answerControl";
    /** Default value of {@link #K_PSTNGSM_ANSWER_CONTROL}. */
    public static final String D_PSTNGSM_ANSWER_CONTROL = "DISABLED";
    /** Property key: PSTN line test frequency (PSTN/GSM module), see {@link PSTNGSM.PSTNLineTestFrequency}. */
    public static final String K_PSTNGSM_PSTN_LINE_TEST_FREQUENCY   = "org.paternostro.elkromm.emulator.PSTNGSM.PSTNLineTestFrequency";
    /** Default value of {@link #K_PSTNGSM_PSTN_LINE_TEST_FREQUENCY}. */
    public static final String D_PSTNGSM_PSTN_LINE_TEST_FREQUENCY   = "DISABLE";
    /** Property key: PSTN answering machine rings (PSTN/GSM module), see {@link PSTNGSM.PSTNAnsweringMachineRings}. */
    public static final String K_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS   = "org.paternostro.elkromm.emulator.PSTNGSM.PSTNAnsweringMachineRings";
    /** Default value of {@link #K_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS}. */
    public static final String D_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS   = "DISABLE";
    /** Property key: enable GSM (PSTN/GSM module), see {@link PSTNGSM.Enabling}. */
    public static final String K_PSTNGSM_ENABLE_GSM = "org.paternostro.elkromm.emulator.PSTNGSM.enableGSM";
    /** Default value of {@link #K_PSTNGSM_ENABLE_GSM}. */
    public static final String D_PSTNGSM_ENABLE_GSM = "DISABLED";
    /** Property key: enable GSM answering machine (PSTN/GSM module), see {@link PSTNGSM.Enabling}. */
    public static final String K_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE   = "org.paternostro.elkromm.emulator.PSTNGSM.enableGSMAnsweringMachine";
    /** Default value of {@link #K_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE}. */
    public static final String D_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE   = "DISABLED";
    /** Property key: enable incoming SMS (PSTN/GSM module), see {@link PSTNGSM.Enabling}. */
    public static final String K_PSTNGSM_ENABLE_INCOMING_SMS    = "org.paternostro.elkromm.emulator.PSTNGSM.enableIncomingSMS";
    /** Default value of {@link #K_PSTNGSM_ENABLE_INCOMING_SMS}. */
    public static final String D_PSTNGSM_ENABLE_INCOMING_SMS    = "DISABLED";
    /** Property key: GSM pin (PSTN/GSM module), {@code -1} for no PIN. */
    public static final String K_PSTNGSM_GSM_PIN    = "org.paternostro.elkromm.emulator.PSTNGSM.GSMPin";
    /** Default value of {@link #K_PSTNGSM_GSM_PIN}. */
    public static final String D_PSTNGSM_GSM_PIN    = "-1";
    /** Property key: expiration month (PSTN/GSM module). */
    public static final String K_PSTNGSM_EXPIRATION_MONTH   = "org.paternostro.elkromm.emulator.PSTNGSM.expirationMonth";
    /** Default value of {@link #K_PSTNGSM_EXPIRATION_MONTH}. */
    public static final String D_PSTNGSM_EXPIRATION_MONTH   = "1";
    /** Property key: expiration year (PSTN/GSM module). */
    public static final String K_PSTNGSM_EXPIRATION_YEAR    = "org.paternostro.elkromm.emulator.PSTNGSM.expirationYear";
    /** Default value of {@link #K_PSTNGSM_EXPIRATION_YEAR}. */
    public static final String D_PSTNGSM_EXPIRATION_YEAR    = "0";

    /** Property key: phone number of a phone number. This is a {@link String#format} pattern, filled with the phone number slot (1-based). */
    public static final String K_PHONE_NUMBERS_PHONE_NUMBER    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.phoneNumber";
    /** Default value of {@link #K_PHONE_NUMBERS_PHONE_NUMBER}. */
    public static final String D_PHONE_NUMBERS_PHONE_NUMBER    = "";
    /** Property key: partitions of a phone number. This is a {@link String#format} pattern, filled with the phone number slot (1-based). */
    public static final String K_PHONE_NUMBERS_PARTITIONS    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.partitions";
    /** Default value of {@link #K_PHONE_NUMBERS_PARTITIONS}. */
    public static final String D_PHONE_NUMBERS_PARTITIONS    = "0";
    /** Property key: type of a phone number, see {@link PhoneNumber.Type}. This is a {@link String#format} pattern, filled with the phone number slot (1-based). */
    public static final String K_PHONE_NUMBERS_TYPE    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.type";
    /** Default value of {@link #K_PHONE_NUMBERS_TYPE}. */
    public static final String D_PHONE_NUMBERS_TYPE    = "PSTN";
    /** Property key: sending mode of a phone number, see {@link PhoneNumber.SendingMode}. This is a {@link String#format} pattern, filled with the phone number slot (1-based). */
    public static final String K_PHONE_NUMBERS_SENDING_MODE    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.sendingMode";
    /** Default value of {@link #K_PHONE_NUMBERS_SENDING_MODE}. */
    public static final String D_PHONE_NUMBERS_SENDING_MODE    = "VOICE";
    /** Property key: assigned events of a phone number, separated by spaces or commas, see {@link PhoneNumber.Event}. This is a {@link String#format} pattern, filled with the phone number slot (1-based). */
    public static final String K_PHONE_NUMBERS_ASSIGNED_EVENTS    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.assignedEvents";
    /** Default value of {@link #K_PHONE_NUMBERS_ASSIGNED_EVENTS}. */
    public static final String D_PHONE_NUMBERS_ASSIGNED_EVENTS    = "";

    /** Property key: SMS text. This is a {@link String#format} pattern, filled with the SMS slot (1-based). */
    public static final String K_SMSES_TEXT     = "org.paternostro.elkromm.emulator.sms.%d";
    /** Default values of {@link #K_SMSES_TEXT}, indexed by the SMS slot number minus one. */
    public static final String[] D_SMSES_TEXT   = {"Burglar", "Technical alarm 1", "Technical alarm 2", "Technical alarm 3", "Fire", "Partition activated", "Partition deactivated", "Tampering", "Notice"};

    /** Property key: reserved (C200b block). */
    public static final String K_C200B_RESERVED = "org.paternostro.elkromm.emulator.c200b.reserved";
    /** Default value of {@link #K_C200B_RESERVED}. */
    public static final String D_C200B_RESERVED = ElkrommUtils.bytesToHex(new byte[C200bParameters.RESERVED_SIZE]);
    /** Property key: event code of the C200b block. This is a {@link String#format} pattern, filled with the event name in camel case. */
    public static final String K_C200B_EVENT_CODES  = "org.paternostro.elkromm.emulator.c200b.event.%s";
    /** Default value of {@link #K_C200B_EVENT_CODES}. */
    public static final String D_C200B_EVENT_CODES  = "255";
    /** Property key: input codes (C200b block). */
    public static final String K_C200B_INPUT_CODES  = "org.paternostro.elkromm.emulator.c200b.inputCodes";
    /** Default value of {@link #K_C200B_INPUT_CODES}. */
    public static final String D_C200B_INPUT_CODES  = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

    // FIXME: input status?
    // FIXME: user enabling vero?

    protected Properties properties;

    Config() {
        this.properties = new Properties();
    }

    /**
     * Replaces the properties this configuration reads from; the argument is copied.
     *
     * @param properties the new properties
     * @throws IllegalArgumentException if {@code properties} is {@code null}
     */
    protected void setProperties(Properties properties)
    {
        if (properties == null) throw new IllegalArgumentException("Properties cannot be null");

        this.properties = (Properties)properties.clone();
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

    public int getAreaPartitions(int area) {
        String  key,
                defaultValue;
        
        if (area < 1 || area > ElkrommFacade.MAX_AREAS) {
            logger.warn("Invalid area number: " + area + ", must be between 1 and " + ElkrommFacade.MAX_AREAS);
            return 0;
        }
        
        key = String.format(K_AREA_PARTITIONS, area);
        defaultValue = D_AREA_PARTITIONS[area - 1];

        return getIntProperty(key, defaultValue, "area " + area + " partitions");
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

    public int getPartitions() {
        return getIntProperty(K_PARTITIONS, D_PARTITIONS, "partitions");
    }

    public Partition.EntryExitDelay getPartitionEntryTime(int partition) {
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return Partition.EntryExitDelay.PD_DISABLED;
        }
        
        return Partition.EntryExitDelay.valueOf("PD_" + properties.getProperty(String.format(K_PARTITION_ENTRY_TIME, partition), D_PARTITION_ENTRY_TIME));
    }

    public Partition.EntryExitDelay getPartitionExitTime(int partition) {
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return Partition.EntryExitDelay.PD_DISABLED;
        }
        
        return Partition.EntryExitDelay.valueOf("PD_" + properties.getProperty(String.format(K_PARTITION_EXIT_TIME, partition), D_PARTITION_EXIT_TIME));
    }

    public String getPartitionName(int partition) {
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return "...";
        }
        
        return properties.getProperty(String.format(K_PARTITION_NAME, partition), D_PARTITION_NAME);
    }

    /**
     * Returns the flag that indicates the presence of a partition vocal name from the property {@link #K_PARTITION_VOCAL_NAME}, or {@link #D_PARTITION_VOCAL_NAME} when the property is not set.
     *
     * @param partition the partition number, between 1 and {@link ElkrommFacade#MAX_PARTITIONS}
     * @return the flag value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public boolean getPartitionVocalName(int partition) {
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return false;
        }
        
        return "true".equalsIgnoreCase(properties.getProperty(String.format(K_PARTITION_VOCAL_NAME, partition), D_PARTITION_VOCAL_NAME));
    }

    public Partition.Type getPartitionType(int partition) {
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return null;
        }
        
        return Partition.Type.valueOf(properties.getProperty(String.format(K_PARTITION_TYPE, partition), D_PARTITION_TYPE));
    }

    /**
     * Returns the user name from the property {@link #K_USER_NAME}, or {@link #D_USER_NAME} when the property is not set.
     *
     * @param user the user number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getUserName(int user) {
        if (user < 1 || user > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid user number: " + user + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_USER_NAME, user), D_USER_NAME[user - 1]);
    }

    /**
     * Returns the user enablings from the property {@link #K_USER_ENABLINGS}, or {@link #D_USER_ENABLINGS} when the property is not set.
     *
     * @param user the user number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getUserEnablings(int user) {
        if (user < 1 || user > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid user number: " + user + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return User.Enabling.DISABLED.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_USER_ENABLINGS, user), D_USER_ENABLINGS[user - 1], "user " + user + " enablings");
    }

    /**
     * Returns the user partitions from the property {@link #K_USER_PARTITIONS}, or {@link #D_USER_PARTITIONS} when the property is not set.
     *
     * @param user the user number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getUserPartitions(int user) {
        if (user < 1 || user > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid user number: " + user + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_USER_PARTITIONS, user), D_USER_PARTITIONS[user - 1], "user " + user + " partitions");
    }

    /**
     * Returns the key name from the property {@link #K_KEY_NAME}, or {@link #D_KEY_NAME} when the property is not set.
     *
     * @param key the key number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getKeyName(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_KEY_NAME, key), D_KEY_NAME);
    }

    /**
     * Returns the key enablings from the property {@link #K_KEY_ENABLINGS}, or {@link #D_KEY_ENABLINGS} when the property is not set.
     *
     * @param key the key number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeyEnablings(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return Key.Enabling.DISABLED.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_KEY_ENABLINGS, key), D_KEY_ENABLINGS, "key " + key + " enablings");
    }

    /**
     * Returns the key specialization from the property {@link #K_KEY_SPECIALIZATION}, or {@link #D_KEY_SPECIALIZATION} when the property is not set.
     *
     * @param key the key number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the configured {@link Key.Specialization} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Key.Specialization getKeySpecialization(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return Key.Specialization.KS_CHANGE_PARTITION_STATUS;
        }
        
        return Key.Specialization.valueOf("KS_" + properties.getProperty(String.format(K_KEY_SPECIALIZATION, key), D_KEY_SPECIALIZATION));
    }

    /**
     * Returns the key partitions from the property {@link #K_KEY_PARTITIONS}, or {@link #D_KEY_PARTITIONS} when the property is not set.
     *
     * @param key the key number, between 1 and {@link ElkrommFacade#MAX_CREDENTIALS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeyPartitions(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_KEY_PARTITIONS, key), D_KEY_PARTITIONS, "key " + key + " partitions");
    }

    /**
     * Returns the expansion name from the property {@link #K_EXPANSION_NAME}, or {@link #D_EXPANSION_NAME} when the property is not set.
     *
     * @param expansion the expansion number, between 0 (the unit embedded in the panel) and {@link ElkrommFacade#MAX_EXPANSIONS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getExpansionName(int expansion) {
        if (expansion < 0 || expansion > ElkrommFacade.MAX_EXPANSIONS) {
            logger.warn("Invalid expansion number: " + expansion + ", must be between 0 and " + ElkrommFacade.MAX_EXPANSIONS);
            return D_EMBEDDED_EXPANSION_NAME;
        }
        
        return properties.getProperty(String.format(K_EXPANSION_NAME, expansion), expansion == 0 ? D_EMBEDDED_EXPANSION_NAME : String.format(D_EXPANSION_NAME, expansion));
    }

    /**
     * Returns the expansion version from the property {@link #K_EXPANSION_VERSION}, or {@link #D_EXPANSION_VERSION} when the property is not set.
     *
     * @param expansion the expansion number, between 0 (the unit embedded in the panel) and {@link ElkrommFacade#MAX_EXPANSIONS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getExpansionVersion(int expansion) {
        if (expansion < 0 || expansion > ElkrommFacade.MAX_EXPANSIONS) {
            logger.warn("Invalid expansion number: " + expansion + ", must be between 0 and " + ElkrommFacade.MAX_EXPANSIONS);
            return D_EMBEDDED_EXPANSION_VERSION;
        }
        
        return properties.getProperty(String.format(K_EXPANSION_VERSION, expansion), expansion == 0 ? D_EMBEDDED_EXPANSION_VERSION : D_EXPANSION_VERSION);
    }

    /**
     * Returns the keypad name from the property {@link #K_KEYPAD_NAME}, or {@link #D_KEYPAD_NAME} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getKeypadName(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_KEYPAD_NAME, keypad), String.format(D_KEYPAD_NAME, keypad));
    }

    /**
     * Returns the keypad version from the property {@link #K_KEYPAD_VERSION}, or {@link #D_KEYPAD_VERSION} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getKeypadVersion(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return D_KEYPAD_VERSION;
        }
        
        return properties.getProperty(String.format(K_KEYPAD_VERSION, keypad), D_KEYPAD_VERSION);
    }

    /**
     * Returns the keypad enablings from the property {@link #K_KEYPAD_ENABLINGS}, or {@link #D_KEYPAD_ENABLINGS} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeypadEnablings(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return (byte)(Keyboard.Enablings.KE_ENTRY.getValue() | Keyboard.Enablings.KE_EXIT.getValue());
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_ENABLINGS, keypad), D_KEYPAD_ENABLINGS, "keypad " + keypad + " enablings");
    }

    /**
     * Returns the keypad partitions from the property {@link #K_KEYPAD_PARTITIONS}, or {@link #D_KEYPAD_PARTITIONS} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeypadPartitions(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_PARTITIONS, keypad), D_KEYPAD_PARTITIONS, "keypad " + keypad + " partitions");
    }

    /**
     * Returns the keypad audio features from the property {@link #K_KEYPAD_AUDIO_FEATURES}, or {@link #D_KEYPAD_AUDIO_FEATURES} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeypadAudioFeatures(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Keyboard.AudioFeatures.KA_NONE.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_AUDIO_FEATURES, keypad), D_KEYPAD_AUDIO_FEATURES, "keypad " + keypad + " audio features");
    }

    /**
     * Returns the keypad input configuration from the property {@link #K_KEYPAD_INPUT_CONFIGURATION}, or {@link #D_KEYPAD_INPUT_CONFIGURATION} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Configuration} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Configuration getKeypadInputConfiguration(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Input.Configuration.IC_NOT_USED;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return Input.Configuration.IC_NOT_USED;
        }
        
        return Input.Configuration.valueOf("IC_" + properties.getProperty(String.format(K_KEYPAD_INPUT_CONFIGURATION, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_CONFIGURATION));
    }

    /**
     * Returns the keypad input specialization from the property {@link #K_KEYPAD_INPUT_SPECIALIZATION}, or {@link #D_KEYPAD_INPUT_SPECIALIZATION} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Specialization} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Specialization getKeypadInputSpecialization(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Input.Specialization.IS_IMMEDIATE;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return Input.Specialization.IS_IMMEDIATE;
        }
        
        return Input.Specialization.valueOf("IS_" + properties.getProperty(String.format(K_KEYPAD_INPUT_SPECIALIZATION, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_SPECIALIZATION));
    }

    /**
     * Returns the keypad input sensitivity from the property {@link #K_KEYPAD_INPUT_SENSITIVITY}, or {@link #D_KEYPAD_INPUT_SENSITIVITY} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Sensitivity} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Sensitivity getKeypadInputSensitivity(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Input.Sensitivity.IS_HIGH;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return Input.Sensitivity.IS_HIGH;
        }
        
        return Input.Sensitivity.valueOf("IS_" + properties.getProperty(String.format(K_KEYPAD_INPUT_SENSITIVITY, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_SENSITIVITY));
    }

    /**
     * Returns the keypad input flags from the property {@link #K_KEYPAD_INPUT_FLAGS}, or {@link #D_KEYPAD_INPUT_FLAGS} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeypadInputFlags(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Input.Flags.IF_NONE.getValue();
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return Input.Flags.IF_NONE.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_INPUT_FLAGS, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_FLAGS, "keypad " + keypad + " " + input.toString().toLowerCase() + " input flags");
    }

    /**
     * Returns the keypad input video from the property {@link #K_KEYPAD_INPUT_VIDEO}, or {@link #D_KEYPAD_INPUT_VIDEO} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Video} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Video getKeypadInputVideo(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Input.Video.IV_NONE;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return Input.Video.IV_NONE;
        }
        
        return Input.Video.valueOf("IV_" + properties.getProperty(String.format(K_KEYPAD_INPUT_VIDEO, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_VIDEO));
    }

    /**
     * Returns the keypad input partitions from the property {@link #K_KEYPAD_INPUT_PARTITIONS}, or {@link #D_KEYPAD_INPUT_PARTITIONS} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getKeypadInputPartitions(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return 0x01;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_INPUT_PARTITIONS, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_PARTITIONS, "keypad " + keypad + " " + input.toString().toLowerCase() + " input partitions");
    }

    /**
     * Returns the keypad input name from the property {@link #K_KEYPAD_INPUT_NAME}, or {@link #D_KEYPAD_INPUT_NAME} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @param inputAddress the logical address of the input, used only to build the default name
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getKeypadInputName(int keypad, InputOrdinal input, int inputAddress) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_KEYPAD_INPUT_NAME, keypad, input.toString().toLowerCase()), String.format(D_KEYPAD_INPUT_NAME, inputAddress));
    }

    /**
     * Returns the keypad input delay from the property {@link #K_KEYPAD_INPUT_DELAY}, or {@link #D_KEYPAD_INPUT_DELAY} when the property is not set.
     *
     * @param keypad the keypad number, between 1 and {@link ElkrommFacade#MAX_KEYPADS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Delay} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Delay getKeypadInputDelay(int keypad, InputOrdinal input) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Input.Delay.ID_5_SECS;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for keypad number: " + keypad);
            return Input.Delay.ID_5_SECS;
        }
        
        return Input.Delay.valueOf("ID_" + properties.getProperty(String.format(K_KEYPAD_INPUT_DELAY, keypad, input.toString().toLowerCase()), D_KEYPAD_INPUT_DELAY));
    }

    /**
     * Returns the reader name from the property {@link #K_READER_NAME}, or {@link #D_READER_NAME} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getReaderName(int reader) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_READER_NAME, reader), String.format(D_READER_NAME, reader));
    }

    /**
     * Returns the reader enablings from the property {@link #K_READER_ENABLINGS}, or {@link #D_READER_ENABLINGS} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getReaderEnablings(int reader) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Reader.Enablings.RE_NONE.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_READER_ENABLINGS, reader), D_READER_ENABLINGS, "reader " + reader + " enablings");
    }

    /**
     * Returns the partition associated with an LED of a reader from the property {@link #K_READER_LED}, or {@link #D_READER_LED} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param led the LED number
     * @return the configured {@link ElkrommFacade.Partition} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public ElkrommFacade.Partition getReaderLed(int reader, int led) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return ElkrommFacade.Partition.P_NONE;
        }
        
        if (led < 1 || led > 4) {
            logger.warn("Invalid led ordinal: " + led + " for reader number: " + reader + ", must be between 1 and 4");
            return ElkrommFacade.Partition.P_NONE;
        }

        // int partition = getIntProperty(String.format(K_READER_LED, reader, led), D_READER_LED, "reader led");

        // if (partition == ElkrommFacade.Partition.P_NONE.getValue()) {
        //     return ElkrommFacade.Partition.P_NONE;
        // }

        // if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
        //     logger.warn("Invalid partition ordinal: " + partition + " led ordinal: " + led + " of reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
        //     return ElkrommFacade.Partition.P_NONE;
        // }

        // partition--;
        
        // return ElkrommFacade.Partition.valueOf((byte)(1 << partition));
        return ElkrommFacade.Partition.valueOf("P_" + properties.getProperty(String.format(K_READER_LED, reader, led), D_READER_LED));
    }

    /**
     * Returns the reader input configuration from the property {@link #K_READER_INPUT_CONFIGURATION}, or {@link #D_READER_INPUT_CONFIGURATION} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Configuration} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Configuration getReaderInputConfiguration(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Input.Configuration.IC_NOT_USED;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return Input.Configuration.IC_NOT_USED;
        }
        
        return Input.Configuration.valueOf("IC_" + properties.getProperty(String.format(K_READER_INPUT_CONFIGURATION, reader, input.toString().toLowerCase()), D_READER_INPUT_CONFIGURATION));
    }

    /**
     * Returns the reader input specialization from the property {@link #K_READER_INPUT_SPECIALIZATION}, or {@link #D_READER_INPUT_SPECIALIZATION} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Specialization} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Specialization getReaderInputSpecialization(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Input.Specialization.IS_IMMEDIATE;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return Input.Specialization.IS_IMMEDIATE;
        }
        
        return Input.Specialization.valueOf("IS_" + properties.getProperty(String.format(K_READER_INPUT_SPECIALIZATION, reader, input.toString().toLowerCase()), D_READER_INPUT_SPECIALIZATION));
    }

    /**
     * Returns the reader input sensitivity from the property {@link #K_READER_INPUT_SENSITIVITY}, or {@link #D_READER_INPUT_SENSITIVITY} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Sensitivity} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Sensitivity getReaderInputSensitivity(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Input.Sensitivity.IS_HIGH;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return Input.Sensitivity.IS_HIGH;
        }
        
        return Input.Sensitivity.valueOf("IS_" + properties.getProperty(String.format(K_READER_INPUT_SENSITIVITY, reader, input.toString().toLowerCase()), D_READER_INPUT_SENSITIVITY));
    }

    /**
     * Returns the reader input flags from the property {@link #K_READER_INPUT_FLAGS}, or {@link #D_READER_INPUT_FLAGS} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getReaderInputFlags(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Input.Flags.IF_NONE.getValue();
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return Input.Flags.IF_NONE.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_READER_INPUT_FLAGS, reader, input.toString().toLowerCase()), D_READER_INPUT_FLAGS, "reader " + reader + " " + input.toString().toLowerCase() + " input flags");
    }

    /**
     * Returns the reader input video from the property {@link #K_READER_INPUT_VIDEO}, or {@link #D_READER_INPUT_VIDEO} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Video} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Video getReaderInputVideo(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Input.Video.IV_NONE;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return Input.Video.IV_NONE;
        }
        
        return Input.Video.valueOf("IV_" + properties.getProperty(String.format(K_READER_INPUT_VIDEO, reader, input.toString().toLowerCase()), D_READER_INPUT_VIDEO));
    }

    /**
     * Returns the reader input partitions from the property {@link #K_READER_INPUT_PARTITIONS}, or {@link #D_READER_INPUT_PARTITIONS} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getReaderInputPartitions(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return 0x01;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_READER_INPUT_PARTITIONS, reader, input.toString().toLowerCase()), D_READER_INPUT_PARTITIONS, "reader " + reader + " " + input.toString().toLowerCase() + " input partitions");
    }

    /**
     * Returns the reader input name from the property {@link #K_READER_INPUT_NAME}, or {@link #D_READER_INPUT_NAME} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @param inputAddress the logical address of the input, used only to build the default name
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getReaderInputName(int reader, InputOrdinal input, int inputAddress) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_READER_INPUT_NAME, reader, input.toString().toLowerCase()), String.format(D_READER_INPUT_NAME, inputAddress));
    }

    /**
     * Returns the reader input delay from the property {@link #K_READER_INPUT_DELAY}, or {@link #D_READER_INPUT_DELAY} when the property is not set.
     *
     * @param reader the reader number, between 1 and {@link ElkrommFacade#MAX_READERS}
     * @param input which of the two inputs of the unit
     * @return the configured {@link Input.Delay} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Input.Delay getReaderInputDelay(int reader, InputOrdinal input) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Input.Delay.ID_5_SECS;
        }
        
        if (input == null) {
            logger.warn("Null input ordinal for reader number: " + reader);
            return Input.Delay.ID_5_SECS;
        }
        
        return Input.Delay.valueOf("ID_" + properties.getProperty(String.format(K_READER_INPUT_DELAY, reader, input.toString().toLowerCase()), D_READER_INPUT_DELAY));
    }

    /**
     * Returns the parameters enablings burglar time from the property {@link #K_PARAMETERS_ENABLINGS_BURGLAR_TIME}, or {@link #D_PARAMETERS_ENABLINGS_BURGLAR_TIME} when the property is not set.
     * @return the configured {@link ParametersEnablings.Time} value
     */
    public ParametersEnablings.Time getParametersEnablingsBurglarTime() {
        return ParametersEnablings.Time.valueOf("PET_" + properties.getProperty(K_PARAMETERS_ENABLINGS_BURGLAR_TIME, D_PARAMETERS_ENABLINGS_BURGLAR_TIME));
    }

    /**
     * Returns the parameters enablings emergency time from the property {@link #K_PARAMETERS_ENABLINGS_EMERGENCY_TIME}, or {@link #D_PARAMETERS_ENABLINGS_EMERGENCY_TIME} when the property is not set.
     * @return the configured {@link ParametersEnablings.Time} value
     */
    public ParametersEnablings.Time getParametersEnablingsEmergencyTime() {
        return ParametersEnablings.Time.valueOf("PET_" + properties.getProperty(K_PARAMETERS_ENABLINGS_EMERGENCY_TIME, D_PARAMETERS_ENABLINGS_EMERGENCY_TIME));
    }

    /**
     * Returns the parameters enablings pre-alarm time from the property {@link #K_PARAMETERS_ENABLINGS_PRE_ALARM_TIME}, or {@link #D_PARAMETERS_ENABLINGS_PRE_ALARM_TIME} when the property is not set.
     * @return the configured {@link ParametersEnablings.Time} value
     */
    public ParametersEnablings.Time getParametersEnablingsPreAlarmTime() {
        return ParametersEnablings.Time.valueOf("PET_" + properties.getProperty(K_PARAMETERS_ENABLINGS_PRE_ALARM_TIME, D_PARAMETERS_ENABLINGS_PRE_ALARM_TIME));
    }

    /**
     * Returns the parameters enablings alarm count from the property {@link #K_PARAMETERS_ENABLINGS_ALARM_COUNT}, or {@link #D_PARAMETERS_ENABLINGS_ALARM_COUNT} when the property is not set.
     * @return the configured {@link ParametersEnablings.AlarmCount} value
     */
    public ParametersEnablings.AlarmCount getParametersEnablingsAlarmCount() {
        return ParametersEnablings.AlarmCount.valueOf("PEAC_" + properties.getProperty(K_PARAMETERS_ENABLINGS_ALARM_COUNT, D_PARAMETERS_ENABLINGS_ALARM_COUNT));
    }

    /**
     * Returns the parameters enablings power lack from the property {@link #K_PARAMETERS_ENABLINGS_POWER_LACK}, or {@link #D_PARAMETERS_ENABLINGS_POWER_LACK} when the property is not set.
     * @return the configured {@link ParametersEnablings.PowerLack} value
     */
    public ParametersEnablings.PowerLack getParametersEnablingsPowerLack() {
        return ParametersEnablings.PowerLack.valueOf("PEPL_" + properties.getProperty(K_PARAMETERS_ENABLINGS_POWER_LACK, D_PARAMETERS_ENABLINGS_POWER_LACK));
    }

    /**
     * Returns the parameters enablings play from the property {@link #K_PARAMETERS_ENABLINGS_PLAY}, or {@link #D_PARAMETERS_ENABLINGS_PLAY} when the property is not set.
     * @return the raw byte value
     */
    public byte getParametersEnablingsPlay() {
        return (byte)getIntProperty(K_PARAMETERS_ENABLINGS_PLAY, D_PARAMETERS_ENABLINGS_PLAY, "play");
    }

    /**
     * Returns the parameters enablings help from the property {@link #K_PARAMETERS_ENABLINGS_HELP}, or {@link #D_PARAMETERS_ENABLINGS_HELP} when the property is not set.
     * @return the raw byte value
     */
    public byte getParametersEnablingsHelp() {
        return (byte)getIntProperty(K_PARAMETERS_ENABLINGS_HELP, D_PARAMETERS_ENABLINGS_HELP, "help");
    }

    /**
     * Returns the parameters enablings LAN from the property {@link #K_PARAMETERS_ENABLINGS_LAN}, or {@link #D_PARAMETERS_ENABLINGS_LAN} when the property is not set.
     * @return the configured {@link ParametersEnablings.Enabling} value
     */
    public ParametersEnablings.Enabling getParametersEnablingsLAN() {
        return ParametersEnablings.Enabling.valueOf("PEE_" + properties.getProperty(K_PARAMETERS_ENABLINGS_LAN, D_PARAMETERS_ENABLINGS_LAN));
    }

    /**
     * Returns the parameters enablings time programmer from the property {@link #K_PARAMETERS_ENABLINGS_TIME_PROGRAMMER}, or {@link #D_PARAMETERS_ENABLINGS_TIME_PROGRAMMER} when the property is not set.
     * @return the configured {@link ParametersEnablings.Enabling} value
     */
    public ParametersEnablings.Enabling getParametersEnablingsTimeProgrammer() {
        return ParametersEnablings.Enabling.valueOf("PEE_" + properties.getProperty(K_PARAMETERS_ENABLINGS_TIME_PROGRAMMER, D_PARAMETERS_ENABLINGS_TIME_PROGRAMMER));
    }

    /**
     * Returns the parameters enablings notice from the property {@link #K_PARAMETERS_ENABLINGS_NOTICE}, or {@link #D_PARAMETERS_ENABLINGS_NOTICE} when the property is not set.
     * @return the configured {@link ParametersEnablings.Notice} value
     */
    public ParametersEnablings.Notice getParametersEnablingsNotice() {
        return ParametersEnablings.Notice.valueOf("PEN_" + properties.getProperty(K_PARAMETERS_ENABLINGS_NOTICE, D_PARAMETERS_ENABLINGS_NOTICE));
    }

    /**
     * Returns the parameters enablings DST from the property {@link #K_PARAMETERS_ENABLINGS_DST}, or {@link #D_PARAMETERS_ENABLINGS_DST} when the property is not set.
     * @return the raw byte value
     */
    public byte getParametersEnablingsDST() {
        return (byte)getIntProperty(K_PARAMETERS_ENABLINGS_DST, D_PARAMETERS_ENABLINGS_DST, "DST");
    }

    /**
     * Returns the parameters enablings dston from the property {@link #K_PARAMETERS_ENABLINGS_DST_ON}, or {@link #D_PARAMETERS_ENABLINGS_DST_ON} when the property is not set.
     * @return the configured {@link ParametersEnablings.Month} value
     */
    public ParametersEnablings.Month getParametersEnablingsDSTOn() {
        return ParametersEnablings.Month.valueOf("PEM_" + properties.getProperty(K_PARAMETERS_ENABLINGS_DST_ON, D_PARAMETERS_ENABLINGS_DST_ON));
    }

    /**
     * Returns the parameters enablings dstoff from the property {@link #K_PARAMETERS_ENABLINGS_DST_OFF}, or {@link #D_PARAMETERS_ENABLINGS_DST_OFF} when the property is not set.
     * @return the configured {@link ParametersEnablings.Month} value
     */
    public ParametersEnablings.Month getParametersEnablingsDSTOff() {
        return ParametersEnablings.Month.valueOf("PEM_" + properties.getProperty(K_PARAMETERS_ENABLINGS_DST_OFF, D_PARAMETERS_ENABLINGS_DST_OFF));
    }

    /**
     * Returns the time programmer command action from the property {@link #K_TIME_PROGRAMMER_COMMAND_ACTION}, or {@link #D_TIME_PROGRAMMER_COMMAND_ACTION} when the property is not set.
     *
     * @param dayClass the day class the command belongs to
     * @param ordinal the slot number (1-based)
     * @return the configured {@link Command.Action} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Command.Action getTimeProgrammerCommandAction(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return Command.Action.CA_NONE;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return Command.Action.CA_NONE;
        }
        
        return Command.Action.valueOf("CA_" + properties.getProperty(String.format(K_TIME_PROGRAMMER_COMMAND_ACTION, ElkrommUtils.toCamelCase(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_ACTION));
    }

    /**
     * Returns the time programmer command object from the property {@link #K_TIME_PROGRAMMER_COMMAND_OBJECT}, or {@link #D_TIME_PROGRAMMER_COMMAND_OBJECT} when the property is not set.
     *
     * @param dayClass the day class the command belongs to
     * @param ordinal the slot number (1-based)
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getTimeProgrammerCommandObject(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return 1;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return 1;
        }
        
        return (byte)getIntProperty(String.format(K_TIME_PROGRAMMER_COMMAND_OBJECT, ElkrommUtils.toCamelCase(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_OBJECT, "command object");
    }

    /**
     * Returns the time programmer command object type from the property {@link #K_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE}, or {@link #D_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE} when the property is not set.
     *
     * @param dayClass the day class the command belongs to
     * @param ordinal the slot number (1-based)
     * @return the configured {@link Command.ObjectType} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Command.ObjectType getTimeProgrammerCommandObjectType(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return null;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return null;
        }

        String  objectTypeName = properties.getProperty(String.format(K_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE, ElkrommUtils.toCamelCase(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE);
        
        return objectTypeName == null ? null : Command.ObjectType.valueOf("COT_" + objectTypeName);
    }

    /**
     * Returns the time programmer command hour from the property {@link #K_TIME_PROGRAMMER_COMMAND_HOUR}, or {@link #D_TIME_PROGRAMMER_COMMAND_HOUR} when the property is not set.
     *
     * @param dayClass the day class the command belongs to
     * @param ordinal the slot number (1-based)
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getTimeProgrammerCommandHour(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return 0;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_TIME_PROGRAMMER_COMMAND_HOUR, ElkrommUtils.toCamelCase(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_HOUR, "command hour");
    }

    /**
     * Returns the time programmer command minute from the property {@link #K_TIME_PROGRAMMER_COMMAND_MINUTE}, or {@link #D_TIME_PROGRAMMER_COMMAND_MINUTE} when the property is not set.
     *
     * @param dayClass the day class the command belongs to
     * @param ordinal the slot number (1-based)
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getTimeProgrammerCommandMinute(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return 0;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_TIME_PROGRAMMER_COMMAND_MINUTE, ElkrommUtils.toCamelCase(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_MINUTE, "command minute");
    }

    /**
     * Returns the time programmer day class from the property {@link #K_TIME_PROGRAMMER_DAY_CLASSES}, or {@link #D_TIME_PROGRAMMER_DAY_CLASSES} when the property is not set.
     *
     * @param weekday the day of the week
     * @return the configured {@link DayClassCommands.DayClass} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public DayClassCommands.DayClass getTimeProgrammerDayClass(Weekday weekday) {
        if (weekday == null) {
            logger.warn("Null weekday");
            return DayClassCommands.DayClass.DCCDC_WORKING_DAY;
        }

        return DayClassCommands.DayClass.valueOf("DCCDC_" + properties.getProperty(String.format(K_TIME_PROGRAMMER_DAY_CLASSES, weekday.toString().toLowerCase()), D_TIME_PROGRAMMER_DAY_CLASSES[weekday.ordinal()]));
    }

    /**
     * Returns the phone parameters call delay from the property {@link #K_PHONE_PARAMETERS_CALL_DELAY}, or {@link #D_PHONE_PARAMETERS_CALL_DELAY} when the property is not set.
     * @return the configured {@link PhoneParameters.Enabling} value
     */
    public PhoneParameters.Enabling getPhoneParametersCallDelay() {
        return PhoneParameters.Enabling.valueOf("PPE_" + properties.getProperty(K_PHONE_PARAMETERS_CALL_DELAY, D_PHONE_PARAMETERS_CALL_DELAY));
    }

    /**
     * Returns the phone parameters return call from the property {@link #K_PHONE_PARAMETERS_RETURN_CALL}, or {@link #D_PHONE_PARAMETERS_RETURN_CALL} when the property is not set.
     * @return the configured {@link PhoneParameters.ReturnCall} value
     */
    public PhoneParameters.ReturnCall getPhoneParametersReturnCall() {
        return PhoneParameters.ReturnCall.valueOf("PPRC_" + properties.getProperty(K_PHONE_PARAMETERS_RETURN_CALL, D_PHONE_PARAMETERS_RETURN_CALL));
    }

    /**
     * Returns the phone parameters remote surveillance from the property {@link #K_PHONE_PARAMETERS_REMOTE_SURVEILLANCE}, or {@link #D_PHONE_PARAMETERS_REMOTE_SURVEILLANCE} when the property is not set.
     * @return the configured {@link PhoneParameters.Enabling} value
     */
    public PhoneParameters.Enabling getPhoneParametersRemoteSurveillance() {
        return PhoneParameters.Enabling.valueOf("PPE_" + properties.getProperty(K_PHONE_PARAMETERS_REMOTE_SURVEILLANCE, D_PHONE_PARAMETERS_REMOTE_SURVEILLANCE));
    }

    /**
     * Returns the phone parameters voice messages sending mode from the property {@link #K_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE}, or {@link #D_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE} when the property is not set.
     * @return the configured {@link PhoneParameters.VoiceMessagesSendingMode} value
     */
    public PhoneParameters.VoiceMessagesSendingMode getPhoneParametersVoiceMessagesSendingMode() {
        return PhoneParameters.VoiceMessagesSendingMode.valueOf("PPVMSM_" + properties.getProperty(K_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE, D_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE));
    }

    /**
     * Returns the phone parameters cyclic test call frequency from the property {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY}, or {@link #D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY} when the property is not set.
     * @return the configured {@link PhoneParameters.CyclicTestCallFrequency} value
     */
    public PhoneParameters.CyclicTestCallFrequency getPhoneParametersCyclicTestCallFrequency() {
        return PhoneParameters.CyclicTestCallFrequency.valueOf("PPCTCF_" + properties.getProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY));
    }

    /**
     * Returns the phone parameters cyclic test call phone number from the property {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER}, or {@link #D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER} when the property is not set.
     * @return the raw byte value
     */
    public byte getPhoneParametersCyclicTestCallPhoneNumber() {
        return (byte)getIntProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER, "phone parameters cyclic test call phone number");
    }

    /**
     * Returns the phone parameters cyclic test call hour from the property {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR}, or {@link #D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR} when the property is not set.
     * @return the raw byte value
     */
    public byte getPhoneParametersCyclicTestCallHour() {
        return (byte)getIntProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR, "phone parameters cyclic test call hour");
    }

    /**
     * Returns the phone parameters cyclic test call minute from the property {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE}, or {@link #D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE} when the property is not set.
     * @return the raw byte value
     */
    public byte getPhoneParametersCyclicTestCallMinute() {
        return (byte)getIntProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE, "phone parameters cyclic test call minute");
    }

    /**
     * Returns the phone parameters cyclic test call interval from the property {@link #K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL}, or {@link #D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL} when the property is not set.
     * @return the configured {@link PhoneParameters.CyclicTestCallInterval} value
     */
    public PhoneParameters.CyclicTestCallInterval getPhoneParametersCyclicTestCallInterval() {
        return PhoneParameters.CyclicTestCallInterval.valueOf("PPCTCI_" + properties.getProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL));
    }

    /**
     * Returns the enable PSTN (PSTN/GSM module) from the property {@link #K_PSTNGSM_ENABLE_PSTN}, or {@link #D_PSTNGSM_ENABLE_PSTN} when the property is not set.
     * @return the configured {@link PSTNGSM.Enabling} value
     */
    public PSTNGSM.Enabling getPSTNGSMEnablePSTN() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_PSTN, D_PSTNGSM_ENABLE_PSTN));
    }

    /**
     * Returns the country (PSTN/GSM module) from the property {@link #K_PSTNGSM_COUNTRY}, or {@link #D_PSTNGSM_COUNTRY} when the property is not set.
     * @return the configured {@link PSTNGSM.Country} value
     */
    public PSTNGSM.Country getPSTNGSMCountry() {
        return PSTNGSM.Country.valueOf("PGC_" + properties.getProperty(K_PSTNGSM_COUNTRY, D_PSTNGSM_COUNTRY));
    }

    /**
     * Returns the PABX local access digit (PSTN/GSM module) from the property {@link #K_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT}, or {@link #D_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT} when the property is not set.
     * @return the configured {@link PSTNGSM.PABXLocalAccessDigit} value
     */
    public PSTNGSM.PABXLocalAccessDigit getPSTNGSMPABXLocalAccessDigit() {
        return PSTNGSM.PABXLocalAccessDigit.valueOf("PGPLAD_" + properties.getProperty(K_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT, D_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT));
    }

    /**
     * Returns the tone control (PSTN/GSM module) from the property {@link #K_PSTNGSM_TONE_CONTROL}, or {@link #D_PSTNGSM_TONE_CONTROL} when the property is not set.
     * @return the configured {@link PSTNGSM.Enabling} value
     */
    public PSTNGSM.Enabling getPSTNGSMToneControl() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_TONE_CONTROL, D_PSTNGSM_TONE_CONTROL));
    }

    /**
     * Returns the answer control (PSTN/GSM module) from the property {@link #K_PSTNGSM_ANSWER_CONTROL}, or {@link #D_PSTNGSM_ANSWER_CONTROL} when the property is not set.
     * @return the configured {@link PSTNGSM.Enabling} value
     */
    public PSTNGSM.Enabling getPSTNGSMAnswerControl() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ANSWER_CONTROL, D_PSTNGSM_ANSWER_CONTROL));
    }

    /**
     * Returns the PSTN line test frequency (PSTN/GSM module) from the property {@link #K_PSTNGSM_PSTN_LINE_TEST_FREQUENCY}, or {@link #D_PSTNGSM_PSTN_LINE_TEST_FREQUENCY} when the property is not set.
     * @return the configured {@link PSTNGSM.PSTNLineTestFrequency} value
     */
    public PSTNGSM.PSTNLineTestFrequency getPSTNGSMPSTNLineTestFrequency() {
        return PSTNGSM.PSTNLineTestFrequency.valueOf("PGPLTF_" + properties.getProperty(K_PSTNGSM_PSTN_LINE_TEST_FREQUENCY, D_PSTNGSM_PSTN_LINE_TEST_FREQUENCY));
    }

    /**
     * Returns the PSTN answering machine rings (PSTN/GSM module) from the property {@link #K_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS}, or {@link #D_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS} when the property is not set.
     * @return the configured {@link PSTNGSM.PSTNAnsweringMachineRings} value
     */
    public PSTNGSM.PSTNAnsweringMachineRings getPSTNGSMPSTNAnsweringMachineRings() {
        return PSTNGSM.PSTNAnsweringMachineRings.valueOf("PGPAMR_" + properties.getProperty(K_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS, D_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS));
    }

    /**
     * Returns the enable GSM (PSTN/GSM module) from the property {@link #K_PSTNGSM_ENABLE_GSM}, or {@link #D_PSTNGSM_ENABLE_GSM} when the property is not set.
     * @return the configured {@link PSTNGSM.Enabling} value
     */
    public PSTNGSM.Enabling getPSTNGSMEnableGSM() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_GSM, D_PSTNGSM_ENABLE_GSM));
    }

    /**
     * Returns the enable GSM answering machine (PSTN/GSM module) from the property {@link #K_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE}, or {@link #D_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE} when the property is not set.
     * @return the configured {@link PSTNGSM.Enabling} value
     */
    public PSTNGSM.Enabling getPSTNGSMEnableGSMAnsweringMachine() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE, D_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE));
    }

    /**
     * Returns the enable incoming SMS (PSTN/GSM module) from the property {@link #K_PSTNGSM_ENABLE_INCOMING_SMS}, or {@link #D_PSTNGSM_ENABLE_INCOMING_SMS} when the property is not set.
     * @return the configured {@link PSTNGSM.Enabling} value
     */
    public PSTNGSM.Enabling getPSTNGSMEnableIncomingSMS() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_INCOMING_SMS, D_PSTNGSM_ENABLE_INCOMING_SMS));
    }

    /**
     * Returns the GSM PIN (PSTN/GSM module) from the property {@link #K_PSTNGSM_GSM_PIN}, or {@link #D_PSTNGSM_GSM_PIN} when the property is not set.
     * @return the numeric value
     */
    public int getPSTNGSMGSMPIN() {
        return getIntProperty(K_PSTNGSM_GSM_PIN, D_PSTNGSM_GSM_PIN, "PSTNGSM GSM PIN");
    }

    /**
     * Returns the expiration month (PSTN/GSM module) from the property {@link #K_PSTNGSM_EXPIRATION_MONTH}, or {@link #D_PSTNGSM_EXPIRATION_MONTH} when the property is not set.
     * @return the raw byte value
     */
    public byte getPSTNGSMExpirationMonth() {
        return (byte)getIntProperty(K_PSTNGSM_EXPIRATION_MONTH, D_PSTNGSM_EXPIRATION_MONTH, "PSTNGSM expiration month");
    }

    /**
     * Returns the expiration year (PSTN/GSM module) from the property {@link #K_PSTNGSM_EXPIRATION_YEAR}, or {@link #D_PSTNGSM_EXPIRATION_YEAR} when the property is not set.
     * @return the raw byte value
     */
    public byte getPSTNGSMExpirationYear() {
        return (byte)getIntProperty(K_PSTNGSM_EXPIRATION_YEAR, D_PSTNGSM_EXPIRATION_YEAR, "PSTNGSM expiration year");
    }

    // private String      phoneNumber;
    // private boolean[]   associatedPartitions;
    // private Type        type;
    // private SendingMode sendingMode;
    // private Set<Event>  assignedEvents;

    /**
     * Returns the phone number phone number from the property {@link #K_PHONE_NUMBERS_PHONE_NUMBER}, or {@link #D_PHONE_NUMBERS_PHONE_NUMBER} when the property is not set.
     *
     * @param ordinal the slot number (1-based)
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getPhoneNumberPhoneNumber(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return "";
        }
        
        return properties.getProperty(String.format(K_PHONE_NUMBERS_PHONE_NUMBER, ordinal), D_PHONE_NUMBERS_PHONE_NUMBER);
    }

    /**
     * Returns the phone number partitions from the property {@link #K_PHONE_NUMBERS_PARTITIONS}, or {@link #D_PHONE_NUMBERS_PARTITIONS} when the property is not set.
     *
     * @param ordinal the slot number (1-based)
     * @return the raw byte value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public byte getPhoneNumberPartitions(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_PHONE_NUMBERS_PARTITIONS, ordinal), D_PHONE_NUMBERS_PARTITIONS, "phone number " + ordinal + " partitions");
    }

    /**
     * Returns the phone number type from the property {@link #K_PHONE_NUMBERS_TYPE}, or {@link #D_PHONE_NUMBERS_TYPE} when the property is not set.
     *
     * @param ordinal the slot number (1-based)
     * @return the configured {@link PhoneNumber.Type} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public PhoneNumber.Type getPhoneNumberType(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return PhoneNumber.Type.PNT_PSTN;
        }
        
        return PhoneNumber.Type.valueOf("PNT_" + properties.getProperty(String.format(K_PHONE_NUMBERS_TYPE, ordinal), D_PHONE_NUMBERS_TYPE));
    }

    /**
     * Returns the phone number sending mode from the property {@link #K_PHONE_NUMBERS_SENDING_MODE}, or {@link #D_PHONE_NUMBERS_SENDING_MODE} when the property is not set.
     *
     * @param ordinal the slot number (1-based)
     * @return the configured {@link PhoneNumber.SendingMode} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public PhoneNumber.SendingMode getPhoneNumberSendingMode(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return PhoneNumber.SendingMode.PNSM_VOICE;
        }
        
        return PhoneNumber.SendingMode.valueOf("PNSM_" + properties.getProperty(String.format(K_PHONE_NUMBERS_SENDING_MODE, ordinal), D_PHONE_NUMBERS_SENDING_MODE));
    }

    /**
     * Returns the phone number assigned events from the property {@link #K_PHONE_NUMBERS_ASSIGNED_EVENTS}, or {@link #D_PHONE_NUMBERS_ASSIGNED_EVENTS} when the property is not set.
     *
     * @param ordinal the slot number (1-based)
     * @return the configured {@link Set} value; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public Set<PhoneNumber.Event> getPhoneNumberAssignedEvents(int ordinal) {
        Set<PhoneNumber.Event>  retval = new HashSet<>();
        
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return retval;
        }

        String events = properties.getProperty(String.format(K_PHONE_NUMBERS_ASSIGNED_EVENTS, ordinal), D_PHONE_NUMBERS_ASSIGNED_EVENTS);
        StringTokenizer st = new StringTokenizer(events, " ,");

        while (st.hasMoreTokens()) {
            retval.add(PhoneNumber.Event.valueOf("PNSCE_" + st.nextToken().toUpperCase()));
        }
        
        return retval;
    }

    /**
     * Returns the smses text from the property {@link #K_SMSES_TEXT}, or {@link #D_SMSES_TEXT} when the property is not set.
     *
     * @param ordinal the slot number (1-based)
     * @return the configured text; when an argument is out of range or null a warning is logged and a neutral value is returned
     */
    public String getSMSesText(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_SMS) {
            logger.warn("Invalid SMS ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_SMS);
            return "";
        }
        
        return properties.getProperty(String.format(K_SMSES_TEXT, ordinal), D_SMSES_TEXT[ordinal-1]);
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
            logger.warn("Invalid output number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
            return null;
        }
        
        return Output.Type.valueOf("OT_" + properties.getProperty(String.format(K_OUTPUT_TYPE, output), D_OUTPUT_TYPE));
    }

    public byte getOutputPartitions(int output) {
        if (output < 1 || output > ElkrommFacade.MAX_LOGICAL_OUTPUTS) {
            logger.warn("Invalid output number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_OUTPUT_PARTITIONS, output), D_OUTPUT_PARTITIONS, "outputs");
    }

    public Output.Specialization getOutputSpecialization(int output) {
        if (output < 1 || output > ElkrommFacade.MAX_LOGICAL_OUTPUTS) {
            logger.warn("Invalid output number: " + output + ", must be between 1 and " + ElkrommFacade.MAX_LOGICAL_OUTPUTS);
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

    /**
     * Returns the opaque block the emulator reports at the start of the C200B payload.
     * <p>
     * The value is read from the {@link #K_C200B_RESERVED} property as hexadecimal digits
     * ({@link C200bParameters#RESERVED_SIZE} bytes; whitespace, commas and colons are ignored).
     * If the property is missing or malformed, a zero-filled block is returned.
     *
     * @return the opaque block, length {@link C200bParameters#RESERVED_SIZE}
     */
    public byte[] getC200bReserved() {
        byte[]  retval;

        try {
            retval = ElkrommUtils.hexToBytes(properties.getProperty(K_C200B_RESERVED, D_C200B_RESERVED));

            if (retval.length == 0) {
                retval = new byte[C200bParameters.RESERVED_SIZE];
            } else if (retval.length != C200bParameters.RESERVED_SIZE) {
                logger.warn("Wrong length for " + K_C200B_RESERVED + ", using zeroes");
                retval = new byte[C200bParameters.RESERVED_SIZE];
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Wrong hex string for " + K_C200B_RESERVED + " (" + e.getMessage() + "), using zeroes", e);
            retval = new byte[C200bParameters.RESERVED_SIZE];
        }

        return retval;
    }

    /**
     * Returns the c200b event codes from the property {@link #K_C200B_EVENT_CODES}, or {@link #D_C200B_EVENT_CODES} when the property is not set.
     * @return the configured {@link Map} value
     */
    public Map<C200bParameters.Event,Byte> getC200bEventCodes() {
        Map<C200bParameters.Event,Byte> retval = new HashMap<>();

        for (C200bParameters.Event pivot: C200bParameters.Event.values()) {
            retval.put(pivot, (byte)(getIntProperty(String.format(K_C200B_EVENT_CODES, ElkrommUtils.toCamelCase(pivot.toString().substring(5))), D_C200B_EVENT_CODES, "C200b event codes")));
        }

        return retval;
    }

    /**
     * Returns the c200b input codes from the property {@link #K_C200B_INPUT_CODES}, or {@link #D_C200B_INPUT_CODES} when the property is not set.
     * @return the configured {@code byte[]} value
     */
    public byte[] getC200bInputCodes() {
        byte[]  retval;

        try {
            retval = ElkrommUtils.hexToBytes(properties.getProperty(K_C200B_INPUT_CODES, D_C200B_INPUT_CODES));

            if (retval.length == 0) {
                retval = new byte[ElkrommFacade.MAX_LOGICAL_INPUTS];
            } else if (retval.length != ElkrommFacade.MAX_LOGICAL_INPUTS) {
                logger.warn("Wrong length for " + K_C200B_INPUT_CODES + ", using zeroes");
                retval = new byte[ElkrommFacade.MAX_LOGICAL_INPUTS];
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Wrong hex string for " + K_C200B_INPUT_CODES + " (" + e.getMessage() + "), using zeroes", e);
            retval = new byte[ElkrommFacade.MAX_LOGICAL_INPUTS];
        }

        return retval;
    }
}
