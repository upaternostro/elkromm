package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public abstract class Credential implements Serializable, Comparable<Credential>
{
    public enum Enabling {
        DISABLED(0x00),
        ENABLED(0x01),
        ALWAYS_ENABLED(0x02),
        UNKNOWN(0xFF);         // should never happen...

        private byte    value;

        Enabling(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static Enabling valueOf(byte value) {
            Enabling    retval = null;

            for (Enabling pivot : Enabling.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    protected int       ordinal;
    protected String    name;
    protected Enabling  enabling;
    protected boolean[] associatedPartitions;

    public Credential(int ordinal, String name, Enabling enabling, boolean[] associatedPartitions)
    {
        setOrdinal(ordinal);
        setName(name);
        setEnabling(enabling);
        setAssociatedPartitions(associatedPartitions);
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(int ordinal)
    {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong ordinal " + ordinal + ", expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.ordinal = ordinal;
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

    public byte getEnablingValue()
    {
        return enabling.getValue();
    }

    public Enabling getEnabling()
    {
        return enabling;
    }

    public void setEnabling(Enabling enabling)
    {
        if (enabling == null) throw new IllegalArgumentException("Missing mandatory enabling");
        if (enabling == Enabling.UNKNOWN) throw new IllegalArgumentException("Wrong enabling UNKNOWN");

        this.enabling = enabling;
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

    @Override
    public String toString()
    {
        return "Credential{" +
                " ordinal=" + ordinal +
                " name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(Credential o) {
        return Integer.compare(this.getOrdinal(), o.getOrdinal());
    }
}
