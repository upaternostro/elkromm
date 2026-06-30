package org.paternostro.elkromm;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.paternostro.elkromm.impl.ElkrommFacadeImpl;
import org.paternostro.elkromm.serializer.AreasAndPartitions;
import org.paternostro.elkromm.serializer.Checksums;
import org.paternostro.elkromm.serializer.Commands;
import org.paternostro.elkromm.serializer.DayClassCommands;
import org.paternostro.elkromm.serializer.ElkrommSerializer;
import org.paternostro.elkromm.serializer.Expansions;
import org.paternostro.elkromm.serializer.Input;
import org.paternostro.elkromm.serializer.Keyboards;
import org.paternostro.elkromm.serializer.Keys;
import org.paternostro.elkromm.serializer.Output;
import org.paternostro.elkromm.serializer.PSTNGSM;
import org.paternostro.elkromm.serializer.ParametersEnablings;
import org.paternostro.elkromm.serializer.PeripheralUnits;
import org.paternostro.elkromm.serializer.PhoneNumber;
import org.paternostro.elkromm.serializer.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.serializer.PhoneParameters;
import org.paternostro.elkromm.serializer.Reader;
import org.paternostro.elkromm.serializer.Readers;
import org.paternostro.elkromm.serializer.SystemStatus;
import org.paternostro.elkromm.serializer.TimeProgrammer;
import org.paternostro.elkromm.serializer.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkrommFactory {
    public static final Logger logger = LoggerFactory.getLogger(ElkrommFactory.class);

    public static final String PROPERTIES_FILE_NAME = "ElkrommFactory.properties";
    public static final String CLASS_NAME_PROPERTY  = "org.paternostro.elkromm.ElkrommFactory.class";
    public static final String CLASS_NAME_DEFAULT   = "org.paternostro.elkromm.ElkrommFactory";

    private static ElkrommFactory   singleton = null;

    public static ElkrommFactory getFactory()
    {
        ElkrommFactory   retval = singleton;

        if (retval == null) {
            synchronized (ElkrommFactory.class) {
                retval = singleton;

                if (retval == null) {
                    Properties p = new Properties();

                    try {
                        InputStream propertiesStream = ElkrommFactory.class.getResourceAsStream(PROPERTIES_FILE_NAME);

                        if (propertiesStream != null) {
                            p.load(propertiesStream);
                        }
                    } catch (IOException e) {
                        logger.warn("Error reading " + PROPERTIES_FILE_NAME + ", using defaults", e);
                    }

                    String className = p.getProperty(CLASS_NAME_PROPERTY, CLASS_NAME_DEFAULT);

                    try {
                        retval = singleton = (ElkrommFactory)Class.forName(className).newInstance();
                        singleton.setProperties(p);
                    } catch (ClassNotFoundException e) {
                        logger.warn("Class " + className + " not found, using defaults", e);
                        retval = singleton = new ElkrommFactory();
                    } catch (InstantiationException e) {
                        logger.warn("Class " + className + " instantiation error, using defaults", e);
                        retval = singleton = new ElkrommFactory();
                    } catch (IllegalAccessException e) {
                        logger.warn("Class " + className + " instantiation error, using defaults", e);
                        retval = singleton = new ElkrommFactory();
                    } catch (ClassCastException e) {
                        logger.warn("Cannot cast class " + className + " to ElkrommFactory, using defaults", e);
                        retval = singleton = new ElkrommFactory();
                    }
                }
            }
        }

        return retval;
    }

    public static final String FACADE_CLASS                         = "org.paternostro.elkromm.ElkrommFactory.ElkrommFacade.class";
    public static final String FACADE_DEFAULT                       = "org.paternostro.elkromm.impl.ElkrommFacadeImpl";

    public static final String AREAS_AND_PARTITIONS_CLASS           = "org.paternostro.elkromm.ElkrommFactory.AreasAndPartitions.class";
    public static final String AREAS_AND_PARTITIONS_DEFAULT         = "org.paternostro.elkromm.serializer.AreasAndPartitions";

    public static final String USERS_CLASS                          = "org.paternostro.elkromm.ElkrommFactory.Users.class";
    public static final String USERS_DEFAULT                        = "org.paternostro.elkromm.serializer.Users";

    public static final String CHECKSUMS_CLASS                      = "org.paternostro.elkromm.ElkrommFactory.Checksums.class";
    public static final String CHECKSUMS_DEFAULT                    = "org.paternostro.elkromm.serializer.Checksums";

    public static final String KEYS_CLASS                           = "org.paternostro.elkromm.ElkrommFactory.Keys.class";
    public static final String KEYS_DEFAULT                         = "org.paternostro.elkromm.serializer.Keys";

    public static final String EXPANSIONS_CLASS                     = "org.paternostro.elkromm.ElkrommFactory.Expansions.class";
    public static final String EXPANSIONS_DEFAULT                   = "org.paternostro.elkromm.serializer.Expansions";

    public static final String PERIPHERAL_UNITS_CLASS               = "org.paternostro.elkromm.ElkrommFactory.PeripheralUnits.class";
    public static final String PERIPHERAL_UNITS_DEFAULT             = "org.paternostro.elkromm.serializer.PeripheralUnits";

    public static final String SYSTEM_STATUS_CLASS                  = "org.paternostro.elkromm.ElkrommFactory.SystemStatus.class";
    public static final String SYSTEM_STATUS_DEFAULT                = "org.paternostro.elkromm.serializer.SystemStatus";

    public static final String INPUT_CLASS                          = "org.paternostro.elkromm.ElkrommFactory.Input.class";
    public static final String INPUT_DEFAULT                        = "org.paternostro.elkromm.serializer.Input";

    public static final String OUTPUT_CLASS                         = "org.paternostro.elkromm.ElkrommFactory.Output.class";
    public static final String OUTPUT_DEFAULT                       = "org.paternostro.elkromm.serializer.Output";

    public static final String KEYBOARDS_CLASS                      = "org.paternostro.elkromm.ElkrommFactory.Keyboards.class";
    public static final String KEYBOARDS_DEFAULT                    = "org.paternostro.elkromm.serializer.Keyboards";

    public static final String READER_CLASS                         = "org.paternostro.elkromm.ElkrommFactory.Reader.class";
    public static final String READER_DEFAULT                       = "org.paternostro.elkromm.serializer.Reader";

    public static final String READERS_CLASS                        = "org.paternostro.elkromm.ElkrommFactory.Readers.class";
    public static final String READERS_DEFAULT                      = "org.paternostro.elkromm.serializer.Readers";

    public static final String PARAMETERS_ENABLINGS_CLASS           = "org.paternostro.elkromm.ElkrommFactory.ParametersEnablings.class";
    public static final String PARAMETERS_ENABLINGS_DEFAULT         = "org.paternostro.elkromm.serializer.ParametersEnablings";

    public static final String COMMANDS_CLASS                       = "org.paternostro.elkromm.ElkrommFactory.Commands.class";
    public static final String COMMANDS_DEFAULT                     = "org.paternostro.elkromm.serializer.Commands";

    public static final String DAY_CLASS_COMMANDS_CLASS             = "org.paternostro.elkromm.ElkrommFactory.DayClassCommands.class";
    public static final String DAY_CLASS_COMMANDS_DEFAULT           = "org.paternostro.elkromm.serializer.DayClassCommands";

    public static final String TIME_PROGRAMMER_CLASS                = "org.paternostro.elkromm.ElkrommFactory.TimeProgrammer.class";
    public static final String TIME_PROGRAMMER_DEFAULT              = "org.paternostro.elkromm.serializer.TimeProgrammer";

    public static final String PHONE_PARAMETERS_CLASS               = "org.paternostro.elkromm.ElkrommFactory.PhoneParameters.class";
    public static final String PHONE_PARAMETERS_DEFAULT             = "org.paternostro.elkromm.serializer.PhoneParameters";

    public static final String PSTN_GSM_CLASS                       = "org.paternostro.elkromm.ElkrommFactory.PSTNGSM.class";
    public static final String PSTN_GSM_DEFAULT                     = "org.paternostro.elkromm.serializer.PSTNGSM";

    public static final String PHONE_NUMBER_CLASS                   = "org.paternostro.elkromm.ElkrommFactory.PhoneNumber.class";
    public static final String PHONE_NUMBER_DEFAULT                 = "org.paternostro.elkromm.serializer.PhoneNumber";

    public static final String PHONE_NUMBERS_SENDING_CODES_CLASS    = "org.paternostro.elkromm.ElkrommFactory.PhoneNumbersSendingCodes.class";
    public static final String PHONE_NUMBERS_SENDING_CODES_DEFAULT  = "org.paternostro.elkromm.serializer.PhoneNumbersSendingCodes";

    private Properties  properties;

    protected ElkrommFactory()
    {
        this.properties = new Properties();
    }

    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public ElkrommFacade getElkrommFacade(InetAddress inetAddr, int port, int plantCode)
    {
        ElkrommFacade   retval = null;
        String          className = properties.getProperty(FACADE_CLASS, FACADE_DEFAULT);

        try {
            retval = (ElkrommFacade)Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn("Class " + className + " not found, using defaults", e);
            retval = new ElkrommFacadeImpl();
        } catch (InstantiationException e) {
            logger.warn("Class " + className + " instantiation error, using defaults", e);
            retval = new ElkrommFacadeImpl();
        } catch (IllegalAccessException e) {
            logger.warn("Class " + className + " instantiation error, using defaults", e);
            retval = new ElkrommFacadeImpl();
        } catch (ClassCastException e) {
            logger.warn("Cannot cast class " + className + " to ElkrommFactory, using defaults", e);
            retval = new ElkrommFacadeImpl();
        }

        retval.init(inetAddr, port, plantCode);

        return retval;
    }

    private ElkrommSerializer<?> getSerializer(String key, String defaultClass)
    {
        ElkrommSerializer<?>    retval = null;
        String                  className = properties.getProperty(key, defaultClass);

        try {
            retval = (ElkrommSerializer<?> )Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn("Class " + className + " not found, using defaults", e);
        } catch (InstantiationException e) {
            logger.warn("Class " + className + " instantiation error, using defaults", e);
        } catch (IllegalAccessException e) {
            logger.warn("Class " + className + " instantiation error, using defaults", e);
        } catch (ClassCastException e) {
            logger.warn("Cannot cast class " + className + " to AreasAndPartitions, using defaults", e);
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions> getAreasAndPartitionsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions>)getSerializer(AREAS_AND_PARTITIONS_CLASS, AREAS_AND_PARTITIONS_DEFAULT);

        if (retval == null) {
            retval = new AreasAndPartitions();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]> getUsersSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>)getSerializer(USERS_CLASS, USERS_DEFAULT);

        if (retval == null) {
            retval = new Users();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Checksums> getChecksumsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Checksums>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Checksums>)getSerializer(CHECKSUMS_CLASS, CHECKSUMS_DEFAULT);

        if (retval == null) {
            retval = new Checksums();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]> getKeysSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>)getSerializer(KEYS_CLASS, KEYS_DEFAULT);

        if (retval == null) {
            retval = new Keys();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Expansion[]> getExpansionsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Expansion[]>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Expansion[]>)getSerializer(EXPANSIONS_CLASS, EXPANSIONS_DEFAULT);

        if (retval == null) {
            retval = new Expansions();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.PeripheralUnits> getPeripheralUnitsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.PeripheralUnits>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.PeripheralUnits>)getSerializer(PERIPHERAL_UNITS_CLASS, PERIPHERAL_UNITS_DEFAULT);

        if (retval == null) {
            retval = new PeripheralUnits();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus> getSystemStatusSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus>)getSerializer(SYSTEM_STATUS_CLASS, SYSTEM_STATUS_DEFAULT);

        if (retval == null) {
            retval = new SystemStatus();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Input> getInputSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Input>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Input>)getSerializer(INPUT_CLASS, INPUT_DEFAULT);

        if (retval == null) {
            retval = new Input();
        }

        return retval;
    }
    
    public ElkrommSerializer<org.paternostro.elkromm.dto.Output> getOutputSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Output>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Output>)getSerializer(OUTPUT_CLASS, OUTPUT_DEFAULT);

        if (retval == null) {
            retval = new Output();
        }

        return retval;
    }
    
    public ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard[]> getKeyboardsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard[]>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard[]>)getSerializer(KEYBOARDS_CLASS, KEYBOARDS_DEFAULT);

        if (retval == null) {
            retval = new Keyboards();
        }

        return retval;
    }
    
    public ElkrommSerializer<org.paternostro.elkromm.dto.Reader> getReaderSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Reader>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Reader>)getSerializer(READER_CLASS, READER_DEFAULT);

        if (retval == null) {
            retval = new Reader();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Reader[]> getReadersSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Reader[]>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Reader[]>)getSerializer(READERS_CLASS, READERS_DEFAULT);

        if (retval == null) {
            retval = new Readers();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings> getParametersEnablingsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings>)getSerializer(PARAMETERS_ENABLINGS_CLASS, PARAMETERS_ENABLINGS_DEFAULT);

        if (retval == null) {
            retval = new ParametersEnablings();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.Command[]> getCommandsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.Command[]>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.Command[]>)getSerializer(COMMANDS_CLASS, COMMANDS_DEFAULT);

        if (retval == null) {
            retval = new Commands();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands> getDayClassCommandsSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands>)getSerializer(DAY_CLASS_COMMANDS_CLASS, DAY_CLASS_COMMANDS_DEFAULT);

        if (retval == null) {
            retval = new DayClassCommands();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer> getTimeProgrammerSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer>)getSerializer(TIME_PROGRAMMER_CLASS, TIME_PROGRAMMER_DEFAULT);

        if (retval == null) {
            retval = new TimeProgrammer();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters> getPhoneParametersSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters>)getSerializer(PHONE_PARAMETERS_CLASS, PHONE_PARAMETERS_DEFAULT);

        if (retval == null) {
            retval = new PhoneParameters();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM> getPSTNGSMSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM>)getSerializer(PSTN_GSM_CLASS, PSTN_GSM_DEFAULT);

        if (retval == null) {
            retval = new PSTNGSM();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber> getPhoneNumberSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>)getSerializer(PHONE_NUMBER_CLASS, PHONE_NUMBER_DEFAULT);

        if (retval == null) {
            retval = new PhoneNumber();
        }

        return retval;
    }

    public ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes> getPhoneNumbersSendingCodesSerializer()
    {
        @SuppressWarnings("unchecked")
        ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes>  retval = (ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes>)getSerializer(PHONE_NUMBERS_SENDING_CODES_CLASS, PHONE_NUMBERS_SENDING_CODES_DEFAULT);

        if (retval == null) {
            retval = new PhoneNumbersSendingCodes();
        }

        return retval;
    }
}
