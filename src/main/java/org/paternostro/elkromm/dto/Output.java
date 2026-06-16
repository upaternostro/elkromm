package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class Output implements Serializable, Comparable<Output>
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

        public static Type toType(byte value) {
            Type  retval = null;

            for (Type pivot : Type.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
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
    private Type            type;
    private boolean[]       associatedPartitions;
    private Specialization  specialization;
    private String          name;

    public Output(int logicNumber, Type type, boolean[] associatedPartitions, Specialization specialization, String name)
    {
        setLogicNumber(logicNumber);
        setType(type);
        setAssociatedPartitions(associatedPartitions);
        setSpecialization(specialization);
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

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        if (type == null) throw new IllegalArgumentException("Missing mandatory type");

        this.type = type;
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

    public Specialization getSpecialization()
    {
        return specialization;
    }

    public void setSpecialization(Specialization specialization)
    {
        if (specialization == null) throw new IllegalArgumentException("Missing mandatory specialization");

        this.specialization = specialization;
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
    public String toString() {
        return "Output{" +
                "name='" + name + '\'' +
                ", logicNumber=" + logicNumber +
                ", type=" + type +
                ", specialization=" + specialization +
                '}';
    }

    @Override
    public int compareTo(Output o)
    {
        return Integer.compare(this.getLogicNumber(), o.getLogicNumber());
    }
}
