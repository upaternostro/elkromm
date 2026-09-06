package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A proximity key reader: reads {@link Key} credentials at a door/partition
 * entry point, with up to four status LEDs each tied to a partition.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Reader implements Serializable, Comparable<Reader>
{
    /**
     * Bitmask of reader enablings.
     * <p>
     * {@code RE_NONE} and {@code RE_ALL} are convenience aliases, not real
     * single-bit values.
     */
    // bitmask!
    public enum Enablings {
        RE_NONE(0x00), // not a real bitmask
        /** Masking detection enabled. */
        RE_MASKING(0x01),
        RE_ALL(0x01); // not a real bitmask

        private byte value;

        Enablings(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this enabling.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Enablings} matching an exact raw bitmask value.
         *
         * @param value the raw value to look up
         * @return the matching enabling, or {@code null} if none matches
         */
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

        /**
         * Checks whether a given enabling's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param enabling the single enabling bit to look for
         * @return {@code true} if {@code enabling}'s bit is set in {@code value}
         */
        public static boolean isEnabled(byte value, Enablings enabling) {
            return (value & enabling.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid enabling bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #RE_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((RE_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
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

    /**
     * Creates a new reader.
     * <p>
     * Unlike other units, a reader is identified by its own {@code address}
     * field, not by a separate index (see {@code READER_PROGRAMMING} in
     * {@code PROTOCOL-ITA.md}).
     *
     * @param address bus address of this reader, greater than 0
     * @param firstInput configuration of the reader's first onboard input
     * @param secondInput configuration of the reader's second onboard input
     * @param led1 partition tied to the first status LED, or {@code null}
     * @param led2 partition tied to the second status LED, or {@code null}
     * @param led3 partition tied to the third status LED, or {@code null}
     * @param led4 partition tied to the fourth status LED, or {@code null}
     * @param enablings a valid {@link Enablings} bitmask
     * @param name display name
     */
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

    /**
     * Returns this reader's bus address.
     *
     * @return the address
     */
    public int getAddress()
    {
        return address;
    }

    /**
     * Sets this reader's bus address.
     *
     * @param address the address to set, greater than 0
     * @throws IllegalArgumentException if not positive
     */
    public void setAddress(int address)
    {
        if (address < 1) throw new IllegalArgumentException("Wrong address " + address + ", expected greater than 0");

        this.address = address;
    }

    /**
     * Returns the configuration of this reader's first onboard input.
     *
     * @return the first input
     */
    public Input getFirstInput()
    {
        return firstInput;
    }

    /**
     * Sets the configuration of this reader's first onboard input.
     *
     * @param firstInput the input to set, not {@code null}
     * @throws IllegalArgumentException if {@code firstInput} is {@code null}
     */
    public void setFirstInput(Input firstInput)
    {
        if (firstInput == null) throw new IllegalArgumentException("Missing mandatory input");

        this.firstInput = firstInput;
    }

    /**
     * Returns the configuration of this reader's second onboard input.
     *
     * @return the second input
     */
    public Input getSecondInput()
    {
        return secondInput;
    }

    /**
     * Sets the configuration of this reader's second onboard input.
     *
     * @param secondInput the input to set, not {@code null}
     * @throws IllegalArgumentException if {@code secondInput} is {@code null}
     */
    public void setSecondInput(Input secondInput)
    {
        if (secondInput == null) throw new IllegalArgumentException("Missing mandatory input");

        this.secondInput = secondInput;
    }

    /**
     * Returns the partition tied to the first status LED.
     *
     * @return the partition, or {@code null} if unused
     */
    public ElkrommFacade.Partition getLed1()
    {
        return led1;
    }

    /**
     * Sets the partition tied to the first status LED.
     *
     * @param led1 the partition to set, or {@code null}
     */
    public void setLed1(ElkrommFacade.Partition led1)
    {
        this.led1 = led1;
    }

    /**
     * Returns the partition tied to the second status LED.
     *
     * @return the partition, or {@code null} if unused
     */
    public ElkrommFacade.Partition getLed2()
    {
        return led2;
    }

    /**
     * Sets the partition tied to the second status LED.
     *
     * @param led2 the partition to set, or {@code null}
     */
    public void setLed2(ElkrommFacade.Partition led2)
    {
        this.led2 = led2;
    }

    /**
     * Returns the partition tied to the third status LED.
     *
     * @return the partition, or {@code null} if unused
     */
    public ElkrommFacade.Partition getLed3()
    {
        return led3;
    }

    /**
     * Sets the partition tied to the third status LED.
     *
     * @param led3 the partition to set, or {@code null}
     */
    public void setLed3(ElkrommFacade.Partition led3)
    {
        this.led3 = led3;
    }

    /**
     * Returns the partition tied to the fourth status LED.
     *
     * @return the partition, or {@code null} if unused
     */
    public ElkrommFacade.Partition getLed4()
    {
        return led4;
    }

    /**
     * Sets the partition tied to the fourth status LED.
     *
     * @param led4 the partition to set, or {@code null}
     */
    public void setLed4(ElkrommFacade.Partition led4)
    {
        this.led4 = led4;
    }

    /**
     * Returns this reader's raw enablings bitmask.
     *
     * @return the {@link Enablings} bitmask
     */
    public byte getEnablings()
    {
        return enablings;
    }

    /**
     * Sets this reader's enablings bitmask.
     *
     * @param enablings a valid {@link Enablings} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setEnablings(byte enablings)
    {
        if (!Enablings.isValid(enablings)) throw new IllegalArgumentException(String.format("Invalid enabling bitmap 0x%02x", enablings));

        this.enablings = enablings;
    }

    /**
     * Returns the display name of this reader.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this reader.
     *
     * @param name the name to set, not {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

        this.name = name;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{address=").append(address).append(", firstInput=").append(firstInput).append(", secondInput=").append(secondInput).append(", led1="
               ).append(led1).append(", led2=").append(led2).append(", led3=").append(led3).append(", led4=").append(led4).append(", enablings=").append(enablings).append(", name="
               ).append(name).append("}");
        
        return sb.toString();
    }

    /**
     * Compares two readers by their bus address.
     *
     * @param o the other reader to compare against
     * @return the result of comparing the two addresses
     */
    @Override
    public int compareTo(Reader o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
