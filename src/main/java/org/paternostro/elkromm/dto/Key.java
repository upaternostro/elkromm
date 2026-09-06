package org.paternostro.elkromm.dto;

/**
 * A proximity key credential: opens/closes partitions (and optionally
 * doors) via a reader, as opposed to a keypad-code {@link User}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Key extends Credential
{
    /** What a key is authorized to do at a reader. */
    public enum Specialization {
        /** No special capability. */
        KS_NONE(0x00),
        /** Can arm/disarm its associated partitions. */
        KS_CHANGE_PARTITION_STATUS(0x01),
        /** Can control access (e.g. open a door) regardless of partition. */
        KS_CTRL_ACCESS(0x02),
        /** Can control access, restricted to its associated partitions. */
        KS_CTRL_ACCESS_PARTITION(0x03);

        private byte value;

        Specialization(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this specialization.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Specialization} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching specialization, or {@code null} if none matches
         */
        public static Specialization valueOf(byte value) {
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

    protected Specialization    specialization;

    /**
     * Creates a new key credential.
     *
     * @param ordinal 1-based position of this key
     * @param name display name
     * @param enabling area/partition enabling flags for this key
     * @param specialization what this key is authorized to do
     * @param associatedPartitions per-partition association flags
     */
    public Key(int ordinal, String name, Enabling enabling, Specialization specialization, boolean[] associatedPartitions)
    {
        super(ordinal, name, enabling, associatedPartitions);

        setSpecialization(specialization);
    }

    /**
     * Returns this key's specialization.
     *
     * @return the specialization
     */
    public Specialization getSpecialization()
    {
        return specialization;
    }

    /**
     * Sets this key's specialization.
     *
     * @param specialization the specialization to set, not {@code null}
     * @throws IllegalArgumentException if {@code specialization} is {@code null}
     */
    public void setSpecialization(Specialization specialization)
    {
        if (specialization == null) throw new IllegalArgumentException("Missing mandatory specialization");

        this.specialization = specialization;
    }

    /**
     * Returns the raw byte value packing both {@link Credential#getEnabling()}
     * and {@link #getSpecialization()} together, as expected on the wire.
     *
     * @return the packed enabling/specialization byte
     */
    public byte getEnablingValue()
    {
        return (byte)(enabling.getValue() | ((specialization.getValue() << 2) & 0x0C));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        
        sb.setLength(sb.length() - 1);
        sb.append(", specialization=").append(specialization).append("}");

        return sb.toString();
    }

}
