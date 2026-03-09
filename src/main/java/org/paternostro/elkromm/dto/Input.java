package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class Input implements Serializable
{
    public enum Configuration {
        IC_NORMALLY_OPEN(0x02),
        IC_NORMALLY_CLOSED_DOUBLE_BALANCED(0x04);

        private byte value;

        Configuration(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }
    }

    public enum Specialization {
        IS_IMMEDIATE(0x00),
        IS_WAY(0x03),
        IS_FIRST_LAST_ENTRY(0x05),
        IS_TECHNO_TYPE_1(0x07);

        private byte value;

        Specialization(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }
    }

    private int             logicNumber;
    private Configuration   configuration;
    private Specialization  specialization;
    private String          name;
    private boolean[]       associatedPartitions;

    public Input(int logicNumber, Configuration configuration, Specialization specialization, boolean[] associatedPartitions, String name)
    {
        this.logicNumber = logicNumber;
        this.configuration = configuration;
        this.specialization = specialization;
        this.associatedPartitions = associatedPartitions;
        this.name = name;
    }

    public int getLogicNumber()
    {
        return logicNumber;
    }

    public void setLogicNumber(int logicNumber)
    {
        this.logicNumber = logicNumber;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public Specialization getSpecialization()
    {
        return specialization;
    }

    public void setSpecialization(Specialization specialization)
    {
        this.specialization = specialization;
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
        return "Input{" +
                "name='" + name + '\'' +
                ", logicNumber=" + logicNumber +
                ", configuration=" + configuration +
                ", specialization=" + specialization +
                '}';
    }
}
