package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class Output implements Serializable
{
    public enum Type {
        OT_NORMALLY_LOW(0x01),
        OT_NORMALLY_HIGH(0x02);

        private byte value;

        Type(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }
    }

    public enum Specialization {
        OS_BURGLAR(0x00),
        OS_PRE_ALARM(0x02),
        OS_TAMPERING(0x03),
        OS_TEL_FAULT(0x0e),
        OS_AND_TC(0x14),
        OS_OR_TC(0x15),
        OS_BURGLAR_TAMPER(0x1d);

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
    private Type            type;
    private boolean[]       associatedPartitions;
    private Specialization  specialization;
    private String          name;

    public Output(int logicNumber, Type type, boolean[] associatedPartitions, Specialization specialization, String name)
    {
        this.logicNumber = logicNumber;
        this.type = type;
        this.associatedPartitions = associatedPartitions;
        this.specialization = specialization;
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

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public boolean[] getAssociatedPartitions()
    {
        return associatedPartitions;
    }

    public void setAssociatedPartitions(boolean[] associatedPartitions)
    {
        this.associatedPartitions = associatedPartitions;
    }

    public Specialization getSpecialization()
    {
        return specialization;
    }

    public void setSpecialization(Specialization specialization)
    {
        this.specialization = specialization;
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
    public String toString() {
        return "Output{" +
                "name='" + name + '\'' +
                ", logicNumber=" + logicNumber +
                ", type=" + type +
                ", specialization=" + specialization +
                '}';
    }
}
