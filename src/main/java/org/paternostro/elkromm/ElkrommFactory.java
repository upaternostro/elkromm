package org.paternostro.elkromm;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.paternostro.elkromm.impl.ElkrommFacadeImpl;
import org.paternostro.elkromm.serializer.AreasAndPartitions;
import org.paternostro.elkromm.serializer.C200bParameters;
import org.paternostro.elkromm.serializer.Checksums;
import org.paternostro.elkromm.serializer.Commands;
import org.paternostro.elkromm.serializer.DayClassCommands;
import org.paternostro.elkromm.serializer.ElkrommSerializer;
import org.paternostro.elkromm.serializer.EnableDisableUser;
import org.paternostro.elkromm.serializer.ExcludeIncludeInput;
import org.paternostro.elkromm.serializer.Expansions;
import org.paternostro.elkromm.serializer.Input;
import org.paternostro.elkromm.serializer.Keyboards;
import org.paternostro.elkromm.serializer.Keys;
import org.paternostro.elkromm.serializer.Output;
import org.paternostro.elkromm.serializer.PSTNGSM;
import org.paternostro.elkromm.serializer.ParametersEnablings;
import org.paternostro.elkromm.serializer.PartitionArming;
import org.paternostro.elkromm.serializer.PeripheralUnits;
import org.paternostro.elkromm.serializer.PhoneNumber;
import org.paternostro.elkromm.serializer.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.serializer.PhoneParameters;
import org.paternostro.elkromm.serializer.Reader;
import org.paternostro.elkromm.serializer.Readers;
import org.paternostro.elkromm.serializer.SMS;
import org.paternostro.elkromm.serializer.SMSs;
import org.paternostro.elkromm.serializer.SingleSMS;
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

    public static final String SMS_CLASS                            = "org.paternostro.elkromm.ElkrommFactory.SMS.class";
    public static final String SMS_DEFAULT                          = "org.paternostro.elkromm.serializer.SMS";

    public static final String SMSs_CLASS                           = "org.paternostro.elkromm.ElkrommFactory.SMSs.class";
    public static final String SMSs_DEFAULT                         = "org.paternostro.elkromm.serializer.SMSs";

    public static final String SINGLE_SMS_CLASS                     = "org.paternostro.elkromm.ElkrommFactory.SingleSMS.class";
    public static final String SINGLE_SMS_DEFAULT                   = "org.paternostro.elkromm.serializer.SingleSMS";

    public static final String C200B_PARAMETERS_CLASS               = "org.paternostro.elkromm.ElkrommFactory.C200bParameters.class";
    public static final String C200B_PARAMETERS_DEFAULT             = "org.paternostro.elkromm.serializer.C200bParameters";

    public static final String PARTITION_ARMING_CLASS               = "org.paternostro.elkromm.ElkrommFactory.PartitionArming.class";
    public static final String PARTITION_ARMING_DEFAULT             = "org.paternostro.elkromm.serializer.PartitionArming";

    public static final String ENABLE_DISABLE_USER_CLASS            = "org.paternostro.elkromm.ElkrommFactory.EnableDisableUser.class";
    public static final String ENABLE_DISABLE_USER_DEFAULT          = "org.paternostro.elkromm.serializer.EnableDisableUser";

    public static final String EXCLUDE_INCLUDE_INPUT_CLASS          = "org.paternostro.elkromm.ElkrommFactory.ExcludeIncludeInput.class";
    public static final String EXCLUDE_INCLUDE_INPUT_DEFAULT        = "org.paternostro.elkromm.serializer.ExcludeIncludeInput";

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

    private ElkrommSerializer<?> getSerializer(String key, String defaultClass, Class<? extends ElkrommSerializer<?>> serializerClass)
    {
        ElkrommSerializer<?>    retval = null;
        String                  className = properties.getProperty(key, defaultClass);

        try {
            retval = (ElkrommSerializer<?>)Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn("Class " + className + " not found, using defaults", e);
        } catch (InstantiationException|IllegalAccessException e) {
            logger.warn("Class " + className + " instantiation error, using defaults", e);
        } catch (ClassCastException e) {
            logger.warn("Cannot cast class " + className + " to ElkrommSerializer<?>, using defaults", e);
        } finally {
            if (retval == null) {
                try {
                    retval = serializerClass.newInstance();
                } catch (InstantiationException|IllegalAccessException e) {
                    logger.error("Class " + serializerClass.getName() + " cannot be instantiated!", e);
                }
            }
        }

        return retval;
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions> getAreasAndPartitionsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions>)getSerializer(AREAS_AND_PARTITIONS_CLASS, AREAS_AND_PARTITIONS_DEFAULT, AreasAndPartitions.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]> getUsersSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>)getSerializer(USERS_CLASS, USERS_DEFAULT, Users.class);

    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Checksums> getChecksumsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Checksums>)getSerializer(CHECKSUMS_CLASS, CHECKSUMS_DEFAULT, Checksums.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]> getKeysSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Credential[]>)getSerializer(KEYS_CLASS, KEYS_DEFAULT, Keys.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Expansion[]> getExpansionsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Expansion[]>)getSerializer(EXPANSIONS_CLASS, EXPANSIONS_DEFAULT, Expansions.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.PeripheralUnits> getPeripheralUnitsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.PeripheralUnits>)getSerializer(PERIPHERAL_UNITS_CLASS, PERIPHERAL_UNITS_DEFAULT, PeripheralUnits.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus> getSystemStatusSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.SystemStatus>)getSerializer(SYSTEM_STATUS_CLASS, SYSTEM_STATUS_DEFAULT, SystemStatus.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Input> getInputSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Input>)getSerializer(INPUT_CLASS, INPUT_DEFAULT, Input.class);
    }
    
    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Output> getOutputSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Output>)getSerializer(OUTPUT_CLASS, OUTPUT_DEFAULT, Output.class);
    }
    
    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard[]> getKeyboardsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard[]>)getSerializer(KEYBOARDS_CLASS, KEYBOARDS_DEFAULT, Keyboards.class);
    }
    
    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Reader> getReaderSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Reader>)getSerializer(READER_CLASS, READER_DEFAULT, Reader.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Reader[]> getReadersSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Reader[]>)getSerializer(READERS_CLASS, READERS_DEFAULT, Readers.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings> getParametersEnablingsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings>)getSerializer(PARAMETERS_ENABLINGS_CLASS, PARAMETERS_ENABLINGS_DEFAULT, ParametersEnablings.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.Command[]> getCommandsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.Command[]>)getSerializer(COMMANDS_CLASS, COMMANDS_DEFAULT, Commands.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands> getDayClassCommandsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.DayClassCommands>)getSerializer(DAY_CLASS_COMMANDS_CLASS, DAY_CLASS_COMMANDS_DEFAULT, DayClassCommands.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer> getTimeProgrammerSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.TimeProgrammer>)getSerializer(TIME_PROGRAMMER_CLASS, TIME_PROGRAMMER_DEFAULT, TimeProgrammer.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters> getPhoneParametersSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.PhoneParameters>)getSerializer(PHONE_PARAMETERS_CLASS, PHONE_PARAMETERS_DEFAULT, PhoneParameters.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM> getPSTNGSMSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.PSTNGSM>)getSerializer(PSTN_GSM_CLASS, PSTN_GSM_DEFAULT, PSTNGSM.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber> getPhoneNumberSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumber>)getSerializer(PHONE_NUMBER_CLASS, PHONE_NUMBER_DEFAULT, PhoneNumber.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes> getPhoneNumbersSendingCodesSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.PhoneNumbersSendingCodes>)getSerializer(PHONE_NUMBERS_SENDING_CODES_CLASS, PHONE_NUMBERS_SENDING_CODES_DEFAULT, PhoneNumbersSendingCodes.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.SMS> getSMSSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.SMS>)getSerializer(SMS_CLASS, SMS_DEFAULT, SMS.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.SMSs> getSMSsSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.SMSs>)getSerializer(SMSs_CLASS, SMSs_DEFAULT, SMSs.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.SingleSMS> getSingleSMSSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.SingleSMS>)getSerializer(SINGLE_SMS_CLASS, SINGLE_SMS_DEFAULT, SingleSMS.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.C200bParameters> getC200bParametersSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.C200bParameters>)getSerializer(C200B_PARAMETERS_CLASS, C200B_PARAMETERS_DEFAULT, C200bParameters.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.PartitionArming> getPartitionArmingSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.PartitionArming>)getSerializer(PARTITION_ARMING_CLASS, PARTITION_ARMING_DEFAULT, PartitionArming.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.EnableDisableUser> getEnableDisableUserSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.EnableDisableUser>)getSerializer(ENABLE_DISABLE_USER_CLASS, ENABLE_DISABLE_USER_DEFAULT, EnableDisableUser.class);
    }

    @SuppressWarnings("unchecked")
    public ElkrommSerializer<org.paternostro.elkromm.dto.ExcludeIncludeInput> getExcludeIncludeInputSerializer()
    {
        return (ElkrommSerializer<org.paternostro.elkromm.dto.ExcludeIncludeInput>)getSerializer(EXCLUDE_INCLUDE_INPUT_CLASS, EXCLUDE_INCLUDE_INPUT_DEFAULT, ExcludeIncludeInput.class);
    }
}
