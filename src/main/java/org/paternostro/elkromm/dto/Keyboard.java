package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class Keyboard implements Serializable, Comparable<Keyboard>
{
    // bitmask!
    public enum Enablings {
        KE_NONE(0x00), // not a real bitmask
        KE_GONG(0x01),
        KE_ENTRY(0x02),
        KE_EXIT(0x04),
        KE_MASKING(0x08),
        KE_FIRE(0x10),
        KE_PANIC(0x20),
        KE_HELP(0x40),
        KE_ALL(0x7F); // not a real bitmask

        private byte value;

        Enablings(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Enablings valueOf(byte value) {
            Enablings  retval = null;

            for (Enablings pivot : Enablings.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        public static boolean isEnabled(byte value, Enablings enabling) {
            return (value & enabling.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((KE_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    // bitmask!
    public enum AudioFeatures {
        KA_NONE(0x00), // not a real bitmask
        KA_CAPABLE(0x02),
        KA_ENABLED(0x04),
        KA_ALL(0x06);

        private byte value;

        AudioFeatures(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static AudioFeatures valueOf(byte value) {
            AudioFeatures  retval = null;

            for (AudioFeatures pivot : AudioFeatures.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        public static boolean is(byte value, AudioFeatures flag) {
            return (value & flag.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((KA_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    private int             address;
    private String          version;
    private Input           firstInput;
    private Input           secondInput;
    private byte            enablings;
    private boolean[]       associatedPartitions;
    private byte            audioFeatures;
    private String          name;

    public Keyboard(int address, String version, Input firstInput, Input secondInput, byte enablings, boolean[] associatedPartitions, byte audioFeatures, String name)
    {
        setAddress(address);
        setVersion(version);
        setFirstInput(firstInput);
        setSecondInput(secondInput);
        setEnablings(enablings);
        setAssociatedPartitions(associatedPartitions);
        setAudioFeatures(audioFeatures);
        setName(name);
    }

    public int getAddress()
    {
        return address;
    }

    public void setAddress(int address)
    {
        if (address < 1) throw new IllegalArgumentException("Wrong address " + address + ", expected greater than 0");

        this.address = address;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        if (version == null) throw new IllegalArgumentException("Missing mandatory version");

        this.version = version;
    }

    public Input getFirstInput()
    {
        return firstInput;
    }

    public void setFirstInput(Input firstInput)
    {
        if (firstInput == null) throw new IllegalArgumentException("Missing mandatory input");

        this.firstInput = firstInput;
    }

    public Input getSecondInput()
    {
        return secondInput;
    }

    public void setSecondInput(Input secondInput)
    {
        if (secondInput == null) throw new IllegalArgumentException("Missing mandatory input");

        this.secondInput = secondInput;
    }

    public byte getEnablings()
    {
        return enablings;
    }

    public void setEnablings(byte enablings)
    {
        if (!Enablings.isValid(enablings)) throw new IllegalArgumentException(String.format("Invalid enabling bitmap 0x%02x", enablings));

        this.enablings = enablings;
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

    public byte getAudioFeatures()
    {
        return audioFeatures;
    }

    public void setAudioFeatures(byte audioFeatures)
    {
        if (!AudioFeatures.isValid(audioFeatures)) throw new IllegalArgumentException(String.format("Invalid audio features bitmap 0x%02x", audioFeatures));

        this.audioFeatures = audioFeatures;
    }

    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

        this.name = name;
    }

    @Override
    public String toString() {
        return "Keyboard{" +
                "name='" + name + '\'' +
                ", address=" + address +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public int compareTo(Keyboard o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
