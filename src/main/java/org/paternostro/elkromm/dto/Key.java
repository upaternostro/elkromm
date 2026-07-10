package org.paternostro.elkromm.dto;

public class Key extends Credential
{
    public enum Specialization {
        KS_NONE(0x00),
        KS_CHANGE_PARTITION_STATUS(0x01),
        KS_CTRL_ACCESS(0x02),
        KS_CTRL_ACCESS_PARTITION(0x03);

        private byte value;

        Specialization(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

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

    public Key(int ordinal, String name, Enabling enabling, Specialization specialization, boolean[] associatedPartitions)
    {
        super(ordinal, name, enabling, associatedPartitions);

        setSpecialization(specialization);
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

    public byte getEnablingValue()
    {
        return (byte)(enabling.getValue() | ((specialization.getValue() << 2) & 0x0C));
    }

    @Override
    public String toString()
    {
        return "Key{" +
                " ordinal=" + ordinal +
                " name='" + name + '\'' +
                '}';
    }
}
