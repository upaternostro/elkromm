package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

public class Reader implements Serializable, Comparable<Reader>
{
    // bitmask!
    public enum Enablings {
        RE_NONE(0x00), // not a real bitmask
        RE_MASKING(0x01),
        RE_ALL(0x01); // not a real bitmask

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
            return (bitmask & (RE_ALL.getValue() ^ 0xFF)) == 0;
        }
    }

    private int                     address;
    private Input                   firstInput;
    private Input                   secondInput;
    private ElkrommFacade.Partition led1;
    private ElkrommFacade.Partition led2;
    private ElkrommFacade.Partition led3;
    private ElkrommFacade.Partition led4;
    private byte                    enablings;
    private String                  name;

    public Reader(int address, Input firstInput, Input secondInput, ElkrommFacade.Partition led1, ElkrommFacade.Partition led2, ElkrommFacade.Partition led3, ElkrommFacade.Partition led4, byte enablings, String name)
    {
        setAddress(address);
        setFirstInput(firstInput);
        setSecondInput(secondInput);
        setLed1(led1);
        setLed2(led2);
        setLed3(led3);
        setLed4(led4);
        setEnablings(enablings);
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

    public ElkrommFacade.Partition getLed1()
    {
        return led1;
    }

    public void setLed1(ElkrommFacade.Partition led1)
    {
        this.led1 = led1;
    }

    public ElkrommFacade.Partition getLed2()
    {
        return led2;
    }

    public void setLed2(ElkrommFacade.Partition led2)
    {
        this.led2 = led2;
    }

    public ElkrommFacade.Partition getLed3()
    {
        return led3;
    }

    public void setLed3(ElkrommFacade.Partition led3)
    {
        this.led3 = led3;
    }

    public ElkrommFacade.Partition getLed4()
    {
        return led4;
    }

    public void setLed4(ElkrommFacade.Partition led4)
    {
        this.led4 = led4;
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
        return "Reader{" +
                "name='" + name + '\'' +
                ", address=" + address +
                '}';
    }

    @Override
    public int compareTo(Reader o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
