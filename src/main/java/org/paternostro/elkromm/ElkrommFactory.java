package org.paternostro.elkromm;

import org.paternostro.elkromm.impl.ElkrommFacadeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

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

    private ElkrommFactory()
    {
    }

    public ElkrommFacade getFacade(InetAddress inetAddr, int port)
    {
        return new ElkrommFacadeImpl(inetAddr, port);
    }
}
