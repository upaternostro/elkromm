package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class User implements Serializable
{
    private boolean     alwaysEnabled;
    private boolean[]   associatedPartitions;
    private String      name;

    public User(boolean alwaysEnabled, boolean[] associatedPartitions, String name)
    {
        this.alwaysEnabled = alwaysEnabled;
        this.associatedPartitions = associatedPartitions;
        this.name = name;
    }

    public boolean isAlwaysEnabled()
    {
        return alwaysEnabled;
    }

    public void setAlwaysEnabled(boolean alwaysEnabled)
    {
        this.alwaysEnabled = alwaysEnabled;
    }

    public boolean[] getAssociatedPartitions()
    {
        return associatedPartitions;
    }

    public void setAssociatedPartitions(boolean[] associatedPartitions)
    {
        this.associatedPartitions = associatedPartitions;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
