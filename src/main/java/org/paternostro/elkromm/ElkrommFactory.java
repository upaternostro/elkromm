package org.paternostro.elkromm;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.paternostro.elkromm.impl.ElkrommFacadeImpl;
import org.paternostro.elkromm.serializer.AreasAndPartitions;
import org.paternostro.elkromm.serializer.Checksums;
import org.paternostro.elkromm.serializer.ElkrommSerializer;
import org.paternostro.elkromm.serializer.Expansions;
import org.paternostro.elkromm.serializer.Keys;
import org.paternostro.elkromm.serializer.PeripheralUnits;
import org.paternostro.elkromm.serializer.SystemStatus;
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

    public static final String FACADE_CLASS                 = "org.paternostro.elkromm.ElkrommFactory.ElkrommFacade.class";
    public static final String FACADE_DEFAULT               = "org.paternostro.elkromm.impl.ElkrommFacadeImpl";

    public static final String AREAS_AND_PARTITIONS_CLASS   = "org.paternostro.elkromm.ElkrommFactory.AreasAndPartitions.class";
    public static final String AREAS_AND_PARTITIONS_DEFAULT = "org.paternostro.elkromm.serializer.AreasAndPartitions";

    public static final String USERS_CLASS                  = "org.paternostro.elkromm.ElkrommFactory.Users.class";
    public static final String USERS_DEFAULT                = "org.paternostro.elkromm.serializer.Users";

    public static final String CHECKSUMS_CLASS              = "org.paternostro.elkromm.ElkrommFactory.Checksums.class";
    public static final String CHECKSUMS_DEFAULT            = "org.paternostro.elkromm.serializer.Checksums";

    public static final String KEYS_CLASS                   = "org.paternostro.elkromm.ElkrommFactory.Keys.class";
    public static final String KEYS_DEFAULT                 = "org.paternostro.elkromm.serializer.Keys";

    public static final String EXPANSIONS_CLASS             = "org.paternostro.elkromm.ElkrommFactory.Expansions.class";
    public static final String EXPANSIONS_DEFAULT           = "org.paternostro.elkromm.serializer.Expansions";

    public static final String PERIPHERAL_UNITS_CLASS       = "org.paternostro.elkromm.ElkrommFactory.PeripheralUnits.class";
    public static final String PERIPHERAL_UNITS_DEFAULT     = "org.paternostro.elkromm.serializer.PeripheralUnits";

    public static final String SYSTEM_STATUS_CLASS          = "org.paternostro.elkromm.ElkrommFactory.SystemStatus.class";
    public static final String SYSTEM_STATUS_DEFAULT        = "org.paternostro.elkromm.serializer.SystemStatus";

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
}
