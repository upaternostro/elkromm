package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class Input implements Serializable, Comparable<Input>
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

        public static Configuration toConfiguration(byte value) {
            Configuration   retval = null;

            for (Configuration pivot : Configuration.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
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

        public static Specialization toSpecialization(byte value) {
            Specialization  retval = null;

            for (Specialization pivot : Specialization.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private int             logicNumber;
    private Configuration   configuration;
    private Specialization  specialization;
    private String          name;
    private boolean[]       associatedPartitions;

    public Input(int logicNumber, Configuration configuration, Specialization specialization, boolean[] associatedPartitions, String name)
    {
        setLogicNumber(logicNumber);
        setConfiguration(configuration);
        setSpecialization(specialization);
        setAssociatedPartitions(associatedPartitions);
        setName(name);
    }

    public int getLogicNumber()
    {
        return logicNumber;
    }

    public void setLogicNumber(int logicNumber)
    {
        if (logicNumber < 1) throw new IllegalArgumentException("Wrong logic number " + logicNumber + ", expected greater than 0");

        this.logicNumber = logicNumber;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        if (configuration == null) throw new IllegalArgumentException("Missing mandatory configuration");

        this.configuration = configuration;
    }

    public Specialization getSpecialization()
    {
        return specialization;
    }

    public void setSpecialization(Specialization specialization)
    {
        if (specialization == null) throw new IllegalArgumentException("Missing mandatory specialization");

        this.specialization = specialization;
    }

    public boolean[] getAssociatedPartitions()
    {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public void setAssociatedPartitions(boolean[] associatedPartitions)
    {
        if (associatedPartitions == null) throw new IllegalArgumentException("Missing mandatory associated partitions");

        this.associatedPartitions = Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

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

    @Override
    public int compareTo(Input o)
    {
        return Integer.compare(this.getLogicNumber(), o.getLogicNumber());
    }
}
