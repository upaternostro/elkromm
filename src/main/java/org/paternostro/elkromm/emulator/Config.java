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
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Command;
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

    protected static volatile Config instance = null;

    public static Config getInstance()
    {
        Config   retval = instance;

        if (retval == null) {
            synchronized (Config.class) {
                retval = instance;

                if (retval == null) {
                    Properties  p = new Properties();

                    try (InputStream propertiesStream = Config.class.getResourceAsStream(PROPERTIES_FILE_NAME)) {
                        if (propertiesStream != null) {
                            p.load(propertiesStream);
                        }
                    } catch (IOException e) {
                        logger.warn("Error reading " + PROPERTIES_FILE_NAME + ", using defaults", e);
                    }

                    String className = p.getProperty(CLASS_NAME_PROPERTY, CLASS_NAME_DEFAULT);

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

    public static final String K_PORT           = "org.paternostro.elkromm.emulator.port";
    public static final String D_PORT           = "8030";
    public static final String K_PLANT_CODE     = "org.paternostro.elkromm.emulator.plant.code";
    public static final String D_PLANT_CODE     = "55555555";
    public static final String K_TECHNICAL_CODE = "org.paternostro.elkromm.emulator.technical.code";
    public static final String D_TECHNICAL_CODE = "000000";
    public static final String K_KEYBOARDS      = "org.paternostro.elkromm.emulator.keyboards";
    public static final String D_KEYBOARDS      = "0";
    public static final String K_READERS        = "org.paternostro.elkromm.emulator.readers";
    public static final String D_READERS        = "0";
    public static final String K_EXPANSIONS     = "org.paternostro.elkromm.emulator.expansions";
    public static final String D_EXPANSIONS     = "0";
    public static final String K_AREAS          = "org.paternostro.elkromm.emulator.areas";
    public static final String D_AREAS          = "0";

    public static final String K_AREA_PARTITIONS    = "org.paternostro.elkromm.emulator.area.%d.partitions";
    public static final String[] D_AREA_PARTITIONS  = {"1", "0", "0", "0"};

    public static final String K_AREA_NAME      = "org.paternostro.elkromm.emulator.area.%d.name";
    public static final String[] D_AREA_NAME    = {"...", ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME};
    
    public static final String K_PARTITIONS     = "org.paternostro.elkromm.emulator.partitions";
    public static final String D_PARTITIONS     = "1";

    public static final String K_PARTITION_ENTRY_TIME   = "org.paternostro.elkromm.emulator.partition.%d.entry.time";
    public static final String[] D_PARTITION_ENTRY_TIME = {"0", "0", "0", "0", "0", "0", "0", "0"};

    public static final String K_PARTITION_EXIT_TIME    = "org.paternostro.elkromm.emulator.partition.%d.exit.time";
    public static final String[] D_PARTITION_EXIT_TIME  = {"0", "0", "0", "0", "0", "0", "0", "0"};

    public static final String K_PARTITION_NAME     = "org.paternostro.elkromm.emulator.partition.%d.name";
    public static final String[] D_PARTITION_NAME   = {ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME, ElkrommFacade.DEFAULT_NAME};

    public static final String K_PARTITION_VOCAL_NAME     = "org.paternostro.elkromm.emulator.partition.%d.vocalName";
    public static final String[] D_PARTITION_VOCAL_NAME   = {"false", "false", "false", "false", "false", "false", "false", "false"};

    public static final String K_PARTITION_TYPE     = "org.paternostro.elkromm.emulator.partition.%d.type";
    public static final String[] D_PARTITION_TYPE   = {"STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD", "STANDARD"};

    public static final String K_USER_NAME      = "org.paternostro.elkromm.emulator.user.%d.name";
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

    public static final String K_USER_ENABLINGS    = "org.paternostro.elkromm.emulator.user.%d.enablings";
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

    public static final String K_USER_PARTITIONS    = "org.paternostro.elkromm.emulator.user.%d.partitions";
    public static final String[] D_USER_PARTITIONS  = { "255", "255", 
                                                        "1", "1", "1", "1", "1", "1", "1", "1",
                                                        "1", "1", "1", "1", "1", "1", "1", "1",
                                                        "1", "1", "1", "1", "1", "1", "1", "1",
                                                        "1", "1", "1", "1", "1", "1" };

    public static final String K_KEY_NAME   = "org.paternostro.elkromm.emulator.key.%d.name";
    public static final String D_KEY_NAME   = ElkrommFacade.DEFAULT_NAME;

    public static final String K_KEY_ENABLINGS  = "org.paternostro.elkromm.emulator.key.%d.enablings";
    public static final String D_KEY_ENABLINGS  = Byte.toString(Key.Enabling.DISABLED.getValue());

    public static final String K_KEY_SPECIALIZATION = "org.paternostro.elkromm.emulator.key.%d.specialization";
    public static final String D_KEY_SPECIALIZATION = "CHANGE_PARTITION_STATUS";

    public static final String K_KEY_PARTITIONS = "org.paternostro.elkromm.emulator.key.%d.partitions";
    public static final String D_KEY_PARTITIONS = "1";

    public static final String K_EXPANSION_NAME             = "org.paternostro.elkromm.emulator.expansion.%d.name";
    public static final String D_EMBEDDED_EXPANSION_NAME    = "UC        ";
    public static final String D_EXPANSION_NAME             = "EP %02d                   ";

    public static final String K_EXPANSION_VERSION          = "org.paternostro.elkromm.emulator.expansion.%d.version";
    public static final String D_EMBEDDED_EXPANSION_VERSION = "0301";
    public static final String D_EXPANSION_VERSION          = "0200";

    public static final String K_INPUT_CONFIGURATION    = "org.paternostro.elkromm.emulator.input.%d.configuration";
    public static final String D_INPUT_CONFIGURATION    = "NORMALLY_CLOSED_DOUBLE_BALANCED";

    public static final String K_INPUT_SPECIALIZATION   = "org.paternostro.elkromm.emulator.input.%d.specialization";
    public static final String D_INPUT_SPECIALIZATION   = "IMMEDIATE";

    public static final String K_INPUT_SENSITIVITY  = "org.paternostro.elkromm.emulator.input.%d.sensitivity";
    public static final String D_INPUT_SENSITIVITY  = "HIGH";

    public static final String K_INPUT_FLAGS    = "org.paternostro.elkromm.emulator.input.%d.flags";
    public static final String D_INPUT_FLAGS    = "0";

    public static final String K_INPUT_VIDEO    = "org.paternostro.elkromm.emulator.input.%d.video";
    public static final String D_INPUT_VIDEO    = "NONE";

    public static final String K_INPUT_PARTITIONS   = "org.paternostro.elkromm.emulator.input.%d.partitions";
    public static final String D_INPUT_PARTITIONS   = "1";

    public static final String K_INPUT_NAME     = "org.paternostro.elkromm.emulator.input.%d.name";
    public static final String D_INPUT_NAME     = "Input %d";

    public static final String K_INPUT_DELAY    = "org.paternostro.elkromm.emulator.input.%d.delay";
    public static final String D_INPUT_DELAY    = "5_SECS";

    public static final String K_OUTPUT_TYPE    = "org.paternostro.elkromm.emulator.output.%d.type";
    public static final String D_OUTPUT_TYPE    = "NORMALLY_LOW";

    public static final String K_OUTPUT_PARTITIONS    = "org.paternostro.elkromm.emulator.output.%d.partitions";
    public static final String D_OUTPUT_PARTITIONS    = "1";

    public static final String K_OUTPUT_SPECIALIZATION    = "org.paternostro.elkromm.emulator.output.%d.specialization";
    public static final String D_OUTPUT_SPECIALIZATION    = "OR_TC";

    public static final String K_OUTPUT_NAME    = "org.paternostro.elkromm.emulator.output.%d.name";
    public static final String D_OUTPUT_NAME    = "Output %d";

    public static final String K_KEYPAD_NAME    = "org.paternostro.elkromm.emulator.keypad.%d.name";
    public static final String D_KEYPAD_NAME    = "KP %02d                   ";

    public static final String K_KEYPAD_VERSION = "org.paternostro.elkromm.emulator.keypad.%d.version";
    public static final String D_KEYPAD_VERSION = "0200";

    public static final String K_KEYPAD_ENABLINGS  = "org.paternostro.elkromm.emulator.keypad.%d.enablings";
    public static final String D_KEYPAD_ENABLINGS  = Byte.toString((byte)(Keyboard.Enablings.KE_ENTRY.getValue() | Keyboard.Enablings.KE_EXIT.getValue()));

    public static final String K_KEYPAD_PARTITIONS = "org.paternostro.elkromm.emulator.keypad.%d.partitions";
    public static final String D_KEYPAD_PARTITIONS = "1";

    public static final String K_KEYPAD_AUDIO_FEATURES = "org.paternostro.elkromm.emulator.keypad.%d.audioFeatures";
    public static final String D_KEYPAD_AUDIO_FEATURES = Byte.toString(Keyboard.AudioFeatures.KA_NONE.getValue());

    public enum InputOrdinal {
        FIRST,
        SECOND
    };

    public static final String K_KEYPAD_INPUT_CONFIGURATION    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.configuration";
    public static final String D_KEYPAD_INPUT_CONFIGURATION    = "NOT_USED";

    public static final String K_KEYPAD_INPUT_SPECIALIZATION   = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.specialization";
    public static final String D_KEYPAD_INPUT_SPECIALIZATION   = "IMMEDIATE";

    public static final String K_KEYPAD_INPUT_SENSITIVITY  = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.sensitivity";
    public static final String D_KEYPAD_INPUT_SENSITIVITY  = "HIGH";

    public static final String K_KEYPAD_INPUT_FLAGS    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.flags";
    public static final String D_KEYPAD_INPUT_FLAGS    = "0";

    public static final String K_KEYPAD_INPUT_VIDEO    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.video";
    public static final String D_KEYPAD_INPUT_VIDEO    = "NONE";

    public static final String K_KEYPAD_INPUT_PARTITIONS   = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.partitions";
    public static final String D_KEYPAD_INPUT_PARTITIONS   = "1";

    public static final String K_KEYPAD_INPUT_NAME     = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.name";
    public static final String D_KEYPAD_INPUT_NAME     = "Input %d";

    public static final String K_KEYPAD_INPUT_DELAY    = "org.paternostro.elkromm.emulator.keypad.%d.%sInput.delay";
    public static final String D_KEYPAD_INPUT_DELAY    = "5_SECS";

    public static final String K_READER_NAME    = "org.paternostro.elkromm.emulator.reader.%d.name";
    public static final String D_READER_NAME    = "DK %02d                   ";

    public static final String K_READER_ENABLINGS  = "org.paternostro.elkromm.emulator.reader.%d.enablings";
    public static final String D_READER_ENABLINGS  = Byte.toString(Reader.Enablings.RE_NONE.getValue());

    public static final String K_READER_LED = "org.paternostro.elkromm.emulator.reader.%d.led%d";
    public static final String D_READER_LED = "NONE";

    public static final String K_READER_INPUT_CONFIGURATION    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.configuration";
    public static final String D_READER_INPUT_CONFIGURATION    = "NOT_USED";

    public static final String K_READER_INPUT_SPECIALIZATION   = "org.paternostro.elkromm.emulator.reader.%d.%sInput.specialization";
    public static final String D_READER_INPUT_SPECIALIZATION   = "IMMEDIATE";

    public static final String K_READER_INPUT_SENSITIVITY  = "org.paternostro.elkromm.emulator.reader.%d.%sInput.sensitivity";
    public static final String D_READER_INPUT_SENSITIVITY  = "HIGH";

    public static final String K_READER_INPUT_FLAGS    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.flags";
    public static final String D_READER_INPUT_FLAGS    = "0";

    public static final String K_READER_INPUT_VIDEO    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.video";
    public static final String D_READER_INPUT_VIDEO    = "NONE";

    public static final String K_READER_INPUT_PARTITIONS   = "org.paternostro.elkromm.emulator.reader.%d.%sInput.partitions";
    public static final String D_READER_INPUT_PARTITIONS   = "1";

    public static final String K_READER_INPUT_NAME     = "org.paternostro.elkromm.emulator.reader.%d.%sInput.name";
    public static final String D_READER_INPUT_NAME     = "Input %d";

    public static final String K_READER_INPUT_DELAY    = "org.paternostro.elkromm.emulator.reader.%d.%sInput.delay";
    public static final String D_READER_INPUT_DELAY    = "5_SECS";

    public static final String K_PARAMETERS_ENABLINGS_BURGLAR_TIME  = "org.paternostro.elkromm.emulator.parametersEnablings.burglarTime";
    public static final String D_PARAMETERS_ENABLINGS_BURGLAR_TIME  = "180_SECS";

    public static final String K_PARAMETERS_ENABLINGS_EMERGENCY_TIME    = "org.paternostro.elkromm.emulator.parametersEnablings.emergencyTime";
    public static final String D_PARAMETERS_ENABLINGS_EMERGENCY_TIME    = "180_SECS";

    public static final String K_PARAMETERS_ENABLINGS_PRE_ALARM_TIME    = "org.paternostro.elkromm.emulator.parametersEnablings.preAlarmTime";
    public static final String D_PARAMETERS_ENABLINGS_PRE_ALARM_TIME    = "180_SECS";

    public static final String K_PARAMETERS_ENABLINGS_ALARM_COUNT   = "org.paternostro.elkromm.emulator.parametersEnablings.alarmCount";
    public static final String D_PARAMETERS_ENABLINGS_ALARM_COUNT   = "NO_COUNT";

    public static final String K_PARAMETERS_ENABLINGS_POWER_LACK    = "org.paternostro.elkromm.emulator.parametersEnablings.powerLack";
    public static final String D_PARAMETERS_ENABLINGS_POWER_LACK    = "1_HOUR";

    public static final String K_PARAMETERS_ENABLINGS_PLAY  = "org.paternostro.elkromm.emulator.parametersEnablings.play";
    public static final String D_PARAMETERS_ENABLINGS_PLAY  = "0";

    public static final String K_PARAMETERS_ENABLINGS_HELP  = "org.paternostro.elkromm.emulator.parametersEnablings.help";
    public static final String D_PARAMETERS_ENABLINGS_HELP  = "0";

    public static final String K_PARAMETERS_ENABLINGS_LAN   = "org.paternostro.elkromm.emulator.parametersEnablings.lan";
    public static final String D_PARAMETERS_ENABLINGS_LAN   = "DISABLE";

    public static final String K_PARAMETERS_ENABLINGS_TIME_PROGRAMMER   = "org.paternostro.elkromm.emulator.parametersEnablings.timeProgrammer";
    public static final String D_PARAMETERS_ENABLINGS_TIME_PROGRAMMER   = "DISABLE";

    public static final String K_PARAMETERS_ENABLINGS_NOTICE    = "org.paternostro.elkromm.emulator.parametersEnablings.notice";
    public static final String D_PARAMETERS_ENABLINGS_NOTICE    = "NO_NOTICE";

    public static final String K_PARAMETERS_ENABLINGS_DST   = "org.paternostro.elkromm.emulator.parametersEnablings.dst";
    public static final String D_PARAMETERS_ENABLINGS_DST   = "0";

    // FIXME: on&off DST da scambiare?
    public static final String K_PARAMETERS_ENABLINGS_DST_ON    = "org.paternostro.elkromm.emulator.parametersEnablings.dst.on";
    public static final String D_PARAMETERS_ENABLINGS_DST_ON    = "OCTOBER";

    public static final String K_PARAMETERS_ENABLINGS_DST_OFF   = "org.paternostro.elkromm.emulator.parametersEnablings.dst.off";
    public static final String D_PARAMETERS_ENABLINGS_DST_OFF   = "MARCH";

    public static final String K_TIME_PROGRAMMER_COMMAND_ACTION = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.action";
    public static final String D_TIME_PROGRAMMER_COMMAND_ACTION = "NONE";

    public static final String K_TIME_PROGRAMMER_COMMAND_OBJECT = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.object";
    public static final String D_TIME_PROGRAMMER_COMMAND_OBJECT = "1";

    public static final String K_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE    = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.objectType";
    // no default for Command.ObjectType: method returns null if not set

    public static final String K_TIME_PROGRAMMER_COMMAND_HOUR   = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.hour";
    public static final String D_TIME_PROGRAMMER_COMMAND_HOUR   = "0";

    public static final String K_TIME_PROGRAMMER_COMMAND_MINUTE = "org.paternostro.elkromm.emulator.timeProgrammer.command.%s.%d.minute";
    public static final String D_TIME_PROGRAMMER_COMMAND_MINUTE = "0";

    public enum Weekday {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    };

    public static final String K_TIME_PROGRAMMER_DAY_CLASSES    = "org.paternostro.elkromm.emulator.timeProgrammer.dayClasses.%s";
    public static final String[] D_TIME_PROGRAMMER_DAY_CLASSES  = {"WORKING_DAY", "WORKING_DAY", "WORKING_DAY", "WORKING_DAY", "WORKING_DAY", "PRE_HOLIDAY", "HOLIDAY"};

    public static final String K_PHONE_PARAMETERS_CALL_DELAY    = "org.paternostro.elkromm.emulator.phoneParameters.callDelay";
    public static final String D_PHONE_PARAMETERS_CALL_DELAY    = "DISABLED";

    public static final String K_PHONE_PARAMETERS_RETURN_CALL   = "org.paternostro.elkromm.emulator.phoneParameters.returnCall";
    public static final String D_PHONE_PARAMETERS_RETURN_CALL   = "DISABLED";

    public static final String K_PHONE_PARAMETERS_REMOTE_SURVEILLANCE   = "org.paternostro.elkromm.emulator.phoneParameters.remoteSurveillance";
    public static final String D_PHONE_PARAMETERS_REMOTE_SURVEILLANCE   = "DISABLED";

    public static final String K_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE    = "org.paternostro.elkromm.emulator.phoneParameters.voiceMessagesSendingMode";
    public static final String D_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE    = "NONE";

    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY    = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallFrequency";
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY    = "DISABLE";

    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallPhoneNumber";
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER = "1";

    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallHour";
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR = "0";

    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE   = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallMinute";
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE   = "0";

    public static final String K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL = "org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallInterval";
    public static final String D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL = "1_HOUR";

    public static final String K_PSTNGSM_ENABLE_PSTN    = "org.paternostro.elkromm.emulator.PSTNGSM.enablePSTN";
    public static final String D_PSTNGSM_ENABLE_PSTN    = "DISABLED";

    public static final String K_PSTNGSM_COUNTRY    = "org.paternostro.elkromm.emulator.PSTNGSM.country";
    public static final String D_PSTNGSM_COUNTRY    = "ITALY";

    public static final String K_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT    = "org.paternostro.elkromm.emulator.PSTNGSM.PABXLocalAccessDigit";
    public static final String D_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT    = "DISABLE";

    public static final String K_PSTNGSM_TONE_CONTROL   = "org.paternostro.elkromm.emulator.PSTNGSM.toneControl";
    public static final String D_PSTNGSM_TONE_CONTROL   = "DISABLED";

    public static final String K_PSTNGSM_ANSWER_CONTROL = "org.paternostro.elkromm.emulator.PSTNGSM.answerControl";
    public static final String D_PSTNGSM_ANSWER_CONTROL = "DISABLED";

    public static final String K_PSTNGSM_PSTN_LINE_TEST_FREQUENCY   = "org.paternostro.elkromm.emulator.PSTNGSM.PSTNLineTestFrequency";
    public static final String D_PSTNGSM_PSTN_LINE_TEST_FREQUENCY   = "DISABLE";

    public static final String K_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS   = "org.paternostro.elkromm.emulator.PSTNGSM.PSTNAnsweringMachineRings";
    public static final String D_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS   = "DISABLE";

    public static final String K_PSTNGSM_ENABLE_GSM = "org.paternostro.elkromm.emulator.PSTNGSM.enableGSM";
    public static final String D_PSTNGSM_ENABLE_GSM = "DISABLED";

    public static final String K_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE   = "org.paternostro.elkromm.emulator.PSTNGSM.enableGSMAnsweringMachine";
    public static final String D_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE   = "DISABLED";

    public static final String K_PSTNGSM_ENABLE_INCOMING_SMS    = "org.paternostro.elkromm.emulator.PSTNGSM.enableIncomingSMS";
    public static final String D_PSTNGSM_ENABLE_INCOMING_SMS    = "DISABLED";

    public static final String K_PSTNGSM_GSM_PIN    = "org.paternostro.elkromm.emulator.PSTNGSM.GSMPin";
    public static final String D_PSTNGSM_GSM_PIN    = "-1";

    public static final String K_PSTNGSM_EXPIRATION_MONTH   = "org.paternostro.elkromm.emulator.PSTNGSM.expirationMonth";
    public static final String D_PSTNGSM_EXPIRATION_MONTH   = "1";

    public static final String K_PSTNGSM_EXPIRATION_YEAR    = "org.paternostro.elkromm.emulator.PSTNGSM.expirationYear";
    public static final String D_PSTNGSM_EXPIRATION_YEAR    = "0";

    public static final String K_PHONE_NUMBERS_PHONE_NUMBER    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.phoneNumber";
    public static final String D_PHONE_NUMBERS_PHONE_NUMBER    = "";

    public static final String K_PHONE_NUMBERS_PARTITIONS    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.partitions";
    public static final String D_PHONE_NUMBERS_PARTITIONS    = "1";

    public static final String K_PHONE_NUMBERS_TYPE    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.type";
    public static final String D_PHONE_NUMBERS_TYPE    = "PSTN";

    public static final String K_PHONE_NUMBERS_SENDING_MODE    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.sendingMode";
    public static final String D_PHONE_NUMBERS_SENDING_MODE    = "VOICE";

    public static final String K_PHONE_NUMBERS_ASSIGNED_EVENTS    = "org.paternostro.elkromm.emulator.phoneNumbers.%d.assignedEvents";
    public static final String D_PHONE_NUMBERS_ASSIGNED_EVENTS    = "";

    public static final String K_SMSES_TEXT     = "org.paternostro.elkromm.emulator.sms.%d";
    public static final String[] D_SMSES_TEXT   = {"Burglar", "Technical alarm 1", "Technical alarm 2", "Technical alarm 3", "Fire", "Partition activated", "Partition deactivated", "Tampering", "Notice"};

    public static final String K_C200B_RESERVED = "org.paternostro.elkromm.emulator.c200b.reserved";
    public static final String D_C200B_RESERVED = "";

    public static final String K_C200B_EVENT_CODES  = "org.paternostro.elkromm.emulator.c200b.event.%s";
    // No default
   
    public static final String K_C200B_INPUT_CODES  = "org.paternostro.elkromm.emulator.c200b.inputCodes";
    public static final String D_C200B_INPUT_CODES  = "";

    // FIXME: input status?
    // FIXME: user enabling vero?

    protected Properties properties;

    Config() {
        this.properties = new Properties();
    }

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

    public int getPartitionEntryTime(int partition) {
        String  key,
                defaultValue;
        
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return 0;
        }
        
        key = String.format(K_PARTITION_ENTRY_TIME, partition);
        defaultValue = D_PARTITION_ENTRY_TIME[partition - 1];

        return getIntProperty(key, defaultValue, "partition " + partition + " entry time");
    }

    public int getPartitionExitTime(int partition) {
        String  key,
                defaultValue;
        
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return 0;
        }
        
        key = String.format(K_PARTITION_EXIT_TIME, partition);
        defaultValue = D_PARTITION_EXIT_TIME[partition - 1];
        
        return getIntProperty(key, defaultValue, "partition " + partition + " exit time");
    }

    public String getPartitionName(int partition) {
        String  key,
                defaultValue;
        
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return "...";
        }
        
        key = String.format(K_PARTITION_NAME, partition);
        defaultValue = D_PARTITION_NAME[partition - 1];
        
        return properties.getProperty(key, defaultValue);
    }

    public boolean getPartitionVocalName(int partition) {
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return false;
        }
        
        return "true".equalsIgnoreCase(properties.getProperty(String.format(K_PARTITION_VOCAL_NAME, partition), D_PARTITION_VOCAL_NAME[partition - 1]));
    }

    public Partition.Type getPartitionType(int partition) {
        String  key,
                defaultValue;
        
        if (partition < 1 || partition > ElkrommFacade.MAX_PARTITIONS) {
            logger.warn("Invalid partition number: " + partition + ", must be between 1 and " + ElkrommFacade.MAX_PARTITIONS);
            return null;
        }
        
        key = String.format(K_PARTITION_TYPE, partition);
        defaultValue = D_PARTITION_TYPE[partition - 1];
        
        return Partition.Type.valueOf(properties.getProperty(key, defaultValue));
    }

    public String getUserName(int user) {
        if (user < 1 || user > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid user number: " + user + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_USER_NAME, user), D_USER_NAME[user - 1]);
    }

    public byte getUserEnablings(int user) {
        if (user < 1 || user > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid user number: " + user + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return User.Enabling.DISABLED.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_USER_ENABLINGS, user), D_USER_ENABLINGS[user - 1], "user " + user + " enablings");
    }

    public byte getUserPartitions(int user) {
        if (user < 1 || user > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid user number: " + user + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_USER_PARTITIONS, user), D_USER_PARTITIONS[user - 1], "user " + user + " partitions");
    }

    public String getKeyName(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_KEY_NAME, key), D_KEY_NAME);
    }

    public byte getKeyEnablings(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return Key.Enabling.DISABLED.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_KEY_ENABLINGS, key), D_KEY_ENABLINGS, "key " + key + " enablings");
    }

    public Key.Specialization getKeySpecialization(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return Key.Specialization.KS_CHANGE_PARTITION_STATUS;
        }
        
        return Key.Specialization.valueOf("KS_" + properties.getProperty(String.format(K_KEY_SPECIALIZATION, key), D_KEY_SPECIALIZATION));
    }

    public byte getKeyPartitions(int key) {
        if (key < 1 || key > ElkrommFacade.MAX_CREDENTIALS) {
            logger.warn("Invalid key number: " + key + ", must be between 1 and " + ElkrommFacade.MAX_CREDENTIALS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_KEY_PARTITIONS, key), D_KEY_PARTITIONS, "key " + key + " partitions");
    }

    public String getExpansionName(int expansion) {
        if (expansion < 0 || expansion > ElkrommFacade.MAX_EXPANSIONS) {
            logger.warn("Invalid expansion number: " + expansion + ", must be between 0 and " + ElkrommFacade.MAX_EXPANSIONS);
            return D_EMBEDDED_EXPANSION_NAME;
        }
        
        return properties.getProperty(String.format(K_EXPANSION_NAME, expansion), expansion == 0 ? D_EMBEDDED_EXPANSION_NAME : String.format(D_EXPANSION_NAME, expansion));
    }

    public String getExpansionVersion(int expansion) {
        if (expansion < 0 || expansion > ElkrommFacade.MAX_EXPANSIONS) {
            logger.warn("Invalid expansion number: " + expansion + ", must be between 0 and " + ElkrommFacade.MAX_EXPANSIONS);
            return D_EMBEDDED_EXPANSION_VERSION;
        }
        
        return properties.getProperty(String.format(K_EXPANSION_VERSION, expansion), expansion == 0 ? D_EMBEDDED_EXPANSION_VERSION : D_EXPANSION_VERSION);
    }

    public String getKeypadName(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_KEYPAD_NAME, keypad), String.format(D_KEYPAD_NAME, keypad));
    }

    public String getKeypadVersion(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return D_KEYPAD_VERSION;
        }
        
        return properties.getProperty(String.format(K_KEYPAD_VERSION, keypad), D_KEYPAD_VERSION);
    }

    public byte getKeypadEnablings(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return (byte)(Keyboard.Enablings.KE_ENTRY.getValue() | Keyboard.Enablings.KE_EXIT.getValue());
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_ENABLINGS, keypad), D_KEYPAD_ENABLINGS, "keypad " + keypad + " enablings");
    }

    public byte getKeypadPartitions(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_PARTITIONS, keypad), D_KEYPAD_PARTITIONS, "keypad " + keypad + " partitions");
    }

    public byte getKeypadAudioFeatures(int keypad) {
        if (keypad < 1 || keypad > ElkrommFacade.MAX_KEYPADS) {
            logger.warn("Invalid keypad number: " + keypad + ", must be between 1 and " + ElkrommFacade.MAX_KEYPADS);
            return Keyboard.AudioFeatures.KA_NONE.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_KEYPAD_AUDIO_FEATURES, keypad), D_KEYPAD_AUDIO_FEATURES, "keypad " + keypad + " audio features");
    }

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

    public String getReaderName(int reader) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return ElkrommFacade.DEFAULT_NAME;
        }
        
        return properties.getProperty(String.format(K_READER_NAME, reader), String.format(D_READER_NAME, reader));
    }

    public byte getReaderEnablings(int reader) {
        if (reader < 1 || reader > ElkrommFacade.MAX_READERS) {
            logger.warn("Invalid reader number: " + reader + ", must be between 1 and " + ElkrommFacade.MAX_READERS);
            return Reader.Enablings.RE_NONE.getValue();
        }
        
        return (byte)getIntProperty(String.format(K_READER_ENABLINGS, reader), D_READER_ENABLINGS, "reader " + reader + " enablings");
    }

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

    public ParametersEnablings.Time getParametersEnablingsBurglarTime() {
        return ParametersEnablings.Time.valueOf("PET_" + properties.getProperty(K_PARAMETERS_ENABLINGS_BURGLAR_TIME, D_PARAMETERS_ENABLINGS_BURGLAR_TIME));
    }

    public ParametersEnablings.Time getParametersEnablingsEmergencyTime() {
        return ParametersEnablings.Time.valueOf("PET_" + properties.getProperty(K_PARAMETERS_ENABLINGS_EMERGENCY_TIME, D_PARAMETERS_ENABLINGS_EMERGENCY_TIME));
    }

    public ParametersEnablings.Time getParametersEnablingsPreAlarmTime() {
        return ParametersEnablings.Time.valueOf("PET_" + properties.getProperty(K_PARAMETERS_ENABLINGS_PRE_ALARM_TIME, D_PARAMETERS_ENABLINGS_PRE_ALARM_TIME));
    }

    public ParametersEnablings.AlarmCount getParametersEnablingsAlarmCount() {
        return ParametersEnablings.AlarmCount.valueOf("PEAC_" + properties.getProperty(K_PARAMETERS_ENABLINGS_ALARM_COUNT, D_PARAMETERS_ENABLINGS_ALARM_COUNT));
    }

    public ParametersEnablings.PowerLack getParametersEnablingsPowerLack() {
        return ParametersEnablings.PowerLack.valueOf("PEPL_" + properties.getProperty(K_PARAMETERS_ENABLINGS_POWER_LACK, D_PARAMETERS_ENABLINGS_POWER_LACK));
    }

    public byte getParametersEnablingsPlay() {
        return (byte)getIntProperty(K_PARAMETERS_ENABLINGS_PLAY, D_PARAMETERS_ENABLINGS_PLAY, "play");
    }

    public byte getParametersEnablingsHelp() {
        return (byte)getIntProperty(K_PARAMETERS_ENABLINGS_HELP, D_PARAMETERS_ENABLINGS_HELP, "help");
    }

    public ParametersEnablings.Enabling getParametersEnablingsLAN() {
        return ParametersEnablings.Enabling.valueOf("PEE_" + properties.getProperty(K_PARAMETERS_ENABLINGS_LAN, D_PARAMETERS_ENABLINGS_LAN));
    }

    public ParametersEnablings.Enabling getParametersEnablingsTimeProgrammer() {
        return ParametersEnablings.Enabling.valueOf("PEE_" + properties.getProperty(K_PARAMETERS_ENABLINGS_TIME_PROGRAMMER, D_PARAMETERS_ENABLINGS_TIME_PROGRAMMER));
    }

    public ParametersEnablings.Notice getParametersEnablingsNotice() {
        return ParametersEnablings.Notice.valueOf("PEN_" + properties.getProperty(K_PARAMETERS_ENABLINGS_NOTICE, D_PARAMETERS_ENABLINGS_NOTICE));
    }

    public byte getParametersEnablingsDST() {
        return (byte)getIntProperty(K_PARAMETERS_ENABLINGS_DST, D_PARAMETERS_ENABLINGS_DST, "DST");
    }

    public ParametersEnablings.Month getParametersEnablingsDSTOn() {
        return ParametersEnablings.Month.valueOf("PEM_" + properties.getProperty(K_PARAMETERS_ENABLINGS_DST_ON, D_PARAMETERS_ENABLINGS_DST_ON));
    }

    public ParametersEnablings.Month getParametersEnablingsDSTOff() {
        return ParametersEnablings.Month.valueOf("PEM_" + properties.getProperty(K_PARAMETERS_ENABLINGS_DST_OFF, D_PARAMETERS_ENABLINGS_DST_OFF));
    }

    private String capitalize(String constantName)
    {
        StringTokenizer st = new StringTokenizer(constantName, "_");
        String          token;
        StringBuffer    sb = new StringBuffer();

        while (st.hasMoreTokens()) {
            token = st.nextToken().toLowerCase();

            if (sb.length() > 0) {
                sb.append(token.substring(0, 1).toUpperCase())
                    .append(token.substring(1));
            } else {
                sb.append(token);
            }
        }

        return sb.toString();
    }

    public Command.Action getTimeProgrammerCommandAction(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return Command.Action.CA_NONE;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return Command.Action.CA_NONE;
        }
        
        return Command.Action.valueOf("CA_" + properties.getProperty(String.format(K_TIME_PROGRAMMER_COMMAND_ACTION, capitalize(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_ACTION));
    }

    public byte getTimeProgrammerCommandObject(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return 1;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return 1;
        }
        
        return (byte)getIntProperty(String.format(K_TIME_PROGRAMMER_COMMAND_OBJECT, capitalize(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_OBJECT, "command object");
    }

    public Command.ObjectType getTimeProgrammerCommandObjectType(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return null;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return null;
        }

        String  objectTypeName = properties.getProperty(String.format(K_TIME_PROGRAMMER_COMMAND_OBJECT_TYPE, capitalize(dayClass.toString().substring(6)), ordinal));
        
        return objectTypeName == null ? null : Command.ObjectType.valueOf("COT_" + objectTypeName);
    }

    public byte getTimeProgrammerCommandHour(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return 0;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_TIME_PROGRAMMER_COMMAND_HOUR, capitalize(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_HOUR, "command hour");
    }

    public byte getTimeProgrammerCommandMinute(DayClass dayClass, int ordinal) {
        if (dayClass == null) {
            logger.warn("Null dayClass");
            return 0;
        }
        
        if (ordinal < 1 || ordinal > ElkrommFacade.NUM_COMMANDS) {
            logger.warn("Invalid command number: " + ordinal + ", must be between 1 and " + ElkrommFacade.NUM_COMMANDS);
            return 0;
        }
        
        return (byte)getIntProperty(String.format(K_TIME_PROGRAMMER_COMMAND_MINUTE, capitalize(dayClass.toString().substring(6)), ordinal), D_TIME_PROGRAMMER_COMMAND_MINUTE, "command minute");
    }

    public DayClassCommands.DayClass getTimeProgrammerDayClass(Weekday weekday) {
        if (weekday == null) {
            logger.warn("Null weekday");
            return DayClassCommands.DayClass.DCCDC_WORKING_DAY;
        }

        return DayClassCommands.DayClass.valueOf("DCCDC_" + properties.getProperty(String.format(K_TIME_PROGRAMMER_DAY_CLASSES, weekday.toString().toLowerCase()), D_TIME_PROGRAMMER_DAY_CLASSES[weekday.ordinal()]));
    }

    public PhoneParameters.Enabling getPhoneParametersCallDelay() {
        return PhoneParameters.Enabling.valueOf("PPE_" + properties.getProperty(K_PHONE_PARAMETERS_CALL_DELAY, D_PHONE_PARAMETERS_CALL_DELAY));
    }

    public PhoneParameters.ReturnCall getPhoneParametersReturnCall() {
        return PhoneParameters.ReturnCall.valueOf("PPRC_" + properties.getProperty(K_PHONE_PARAMETERS_RETURN_CALL, D_PHONE_PARAMETERS_RETURN_CALL));
    }

    public PhoneParameters.Enabling getPhoneParametersRemoteSurveillance() {
        return PhoneParameters.Enabling.valueOf("PPE_" + properties.getProperty(K_PHONE_PARAMETERS_REMOTE_SURVEILLANCE, D_PHONE_PARAMETERS_REMOTE_SURVEILLANCE));
    }

    public PhoneParameters.VoiceMessagesSendingMode getPhoneParametersVoiceMessagesSendingMode() {
        return PhoneParameters.VoiceMessagesSendingMode.valueOf("PPVMSM_" + properties.getProperty(K_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE, D_PHONE_PARAMETERS_VOICE_MESSAGE_SENDING_MODE));
    }

    public PhoneParameters.CyclicTestCallFrequency getPhoneParametersCyclicTestCallFrequency() {
        return PhoneParameters.CyclicTestCallFrequency.valueOf("PPCTCF_" + properties.getProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_FREQUENCY));
    }

    public byte getPhoneParametersCyclicTestCallPhoneNumber() {
        return (byte)getIntProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_PHONE_NUMBER, "phone parameters cyclic test call phone number");
    }

    public byte getPhoneParametersCyclicTestCallHour() {
        return (byte)getIntProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_HOUR, "phone parameters cyclic test call hour");
    }

    public byte getPhoneParametersCyclicTestCallMinute() {
        return (byte)getIntProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_MINUTE, "phone parameters cyclic test call minute");
    }

    public PhoneParameters.CyclicTestCallInterval getPhoneParametersCyclicTestCallInterval() {
        return PhoneParameters.CyclicTestCallInterval.valueOf("PPCTCI_" + properties.getProperty(K_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL, D_PHONE_PARAMETERS_CYCLIC_TEST_CALL_INTERVAL));
    }

    public PSTNGSM.Enabling getPSTNGSMEnablePSTN() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_PSTN, D_PSTNGSM_ENABLE_PSTN));
    }

    public PSTNGSM.Country getPSTNGSMCountry() {
        return PSTNGSM.Country.valueOf("PGC_" + properties.getProperty(K_PSTNGSM_COUNTRY, D_PSTNGSM_COUNTRY));
    }

    public PSTNGSM.PABXLocalAccessDigit getPSTNGSMPABXLocalAccessDigit() {
        return PSTNGSM.PABXLocalAccessDigit.valueOf("PGPLAD_" + properties.getProperty(K_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT, D_PSTNGSM_PABX_LOCAL_ACCESS_DIGIT));
    }

    public PSTNGSM.Enabling getPSTNGSMToneControl() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_TONE_CONTROL, D_PSTNGSM_TONE_CONTROL));
    }

    public PSTNGSM.Enabling getPSTNGSMAnswerControl() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ANSWER_CONTROL, D_PSTNGSM_ANSWER_CONTROL));
    }

    public PSTNGSM.PSTNLineTestFrequency getPSTNGSMPSTNLineTestFrequency() {
        return PSTNGSM.PSTNLineTestFrequency.valueOf("PGPLTF_" + properties.getProperty(K_PSTNGSM_PSTN_LINE_TEST_FREQUENCY, D_PSTNGSM_PSTN_LINE_TEST_FREQUENCY));
    }

    public PSTNGSM.PSTNAnsweringMachineRings getPSTNGSMPSTNAnsweringMachineRings() {
        return PSTNGSM.PSTNAnsweringMachineRings.valueOf("PGPAMR_" + properties.getProperty(K_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS, D_PSTNGSM_PSTN_ANSWERING_MACHINE_RINGS));
    }

    public PSTNGSM.Enabling getPSTNGSMEnableGSM() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_GSM, D_PSTNGSM_ENABLE_GSM));
    }

    public PSTNGSM.Enabling getPSTNGSMEnableGSMAnsweringMachine() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE, D_PSTNGSM_ENABLE_GSM_ANSWERING_MACHINE));
    }

    public PSTNGSM.Enabling getPSTNGSMEnableIncomingSMS() {
        return PSTNGSM.Enabling.valueOf("PGE_" + properties.getProperty(K_PSTNGSM_ENABLE_INCOMING_SMS, D_PSTNGSM_ENABLE_INCOMING_SMS));
    }

    public int getPSTNGSMGSMPIN() {
        return getIntProperty(K_PSTNGSM_GSM_PIN, D_PSTNGSM_GSM_PIN, "PSTNGSM GSM PIN");
    }

    public byte getPSTNGSMExpirationMonth() {
        return (byte)getIntProperty(K_PSTNGSM_EXPIRATION_MONTH, D_PSTNGSM_EXPIRATION_MONTH, "PSTNGSM expiration month");
    }

    public byte getPSTNGSMExpirationYear() {
        return (byte)getIntProperty(K_PSTNGSM_EXPIRATION_YEAR, D_PSTNGSM_EXPIRATION_YEAR, "PSTNGSM expiration year");
    }

    // private String      phoneNumber;
    // private boolean[]   associatedPartitions;
    // private Type        type;
    // private SendingMode sendingMode;
    // private Set<Event>  assignedEvents;

    public String getPhoneNumberPhoneNumber(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return "";
        }
        
        return properties.getProperty(String.format(K_PHONE_NUMBERS_PHONE_NUMBER, ordinal), D_PHONE_NUMBERS_PHONE_NUMBER);
    }

    public byte getPhoneNumberPartitions(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return 0x01;
        }
        
        return (byte)getIntProperty(String.format(K_PHONE_NUMBERS_PARTITIONS, ordinal), D_PHONE_NUMBERS_PARTITIONS, "phone number " + ordinal + " partitions");
    }

    public PhoneNumber.Type getPhoneNumberType(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return PhoneNumber.Type.PNT_PSTN;
        }
        
        return PhoneNumber.Type.valueOf("PNT_" + properties.getProperty(String.format(K_PHONE_NUMBERS_TYPE, ordinal), D_PHONE_NUMBERS_TYPE));
    }

    public PhoneNumber.SendingMode getPhoneNumberSendingMode(int ordinal) {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_PHONE_NUMBERS) {
            logger.warn("Invalid phone number ordinal: " + ordinal + ", must be between 1 and " + ElkrommFacade.MAX_PHONE_NUMBERS);
            return PhoneNumber.SendingMode.PNSM_VOICE;
        }
        
        return PhoneNumber.SendingMode.valueOf("PNSM_" + properties.getProperty(String.format(K_PHONE_NUMBERS_SENDING_MODE, ordinal), D_PHONE_NUMBERS_SENDING_MODE));
    }

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
        return parseC200bReserved(properties.getProperty(K_C200B_RESERVED, D_C200B_RESERVED), C200bParameters.RESERVED_SIZE);
    }

    public Map<C200bParameters.Event,Byte> getC200bEventCodes() {
        Map<C200bParameters.Event,Byte> retval = new HashMap<>();
        String                          pValue;

        for (C200bParameters.Event pivot: C200bParameters.Event.values()) {
            pValue = properties.getProperty(String.format(K_C200B_EVENT_CODES, capitalize(pivot.toString().substring(5))));

            if (pValue != null) {
                retval.put(pivot, Byte.valueOf(pValue));
            }
        }

        return retval;
    }

    public byte[] getC200bInputCodes() {
        return parseC200bReserved(properties.getProperty(K_C200B_INPUT_CODES, D_C200B_INPUT_CODES), ElkrommFacade.MAX_LOGICAL_INPUTS);
    }

    static byte[] parseC200bReserved(String value, int size) {
        byte[]  retval = new byte[size];
        String  hex = value.replaceAll("[\\s,:]", "");

        if (hex.isEmpty()) {
            return retval;
        }

        if (hex.length() != 2 * retval.length || !hex.matches("[0-9a-fA-F]+")) {
            logger.warn("Invalid C200b reserved bytes, expected " + retval.length + " bytes as hexadecimal digits, using zeros");
            return retval;
        }

        for (int i = 0; i < retval.length; i++) {
            retval[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }

        return retval;
    }
}
