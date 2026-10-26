package org.paternostro.elkromm.emulator;

import java.util.Properties;

public class InjectableConfig extends Config
{
    private Properties snapshot = null;

    // change method visibility
    @Override
    public void setProperties(Properties properties)
    {
        super.setProperties(properties);
    }

    public void reset()
    {
        super.setProperties(new Properties());
    }

    public void snapshot()
    {
        snapshot = (Properties)properties.clone();
    }

    public void restore()
    {
        if (snapshot == null) throw new IllegalArgumentException("Snapshot cannot be null");

        properties = (Properties)snapshot.clone();
    }
}
