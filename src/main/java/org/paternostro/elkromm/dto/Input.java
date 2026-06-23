package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class Input implements Serializable, Comparable<Input>
{
    public enum Configuration {
        IC_NOT_USED(0x00),
        IC_NORMALLY_CLOSED(0x01),
        IC_NORMALLY_OPEN(0x02),
        IC_NORMALLY_CLOSED_BALANCED(0x03),
        IC_NORMALLY_CLOSED_DOUBLE_BALANCED(0x04),
        IC_SHOCK(0x07),
        IC_ROLLER(0x08);

        private byte value;

        Configuration(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Configuration valueOf(byte value) {
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
        IS_DELAYED(0x01),
        IS_FIRST_ENTRY(0x02),
        IS_WAY(0x03),
        IS_LAST_EXIT(0x04),
        IS_FIRST_ENTRY_LAST_EXIT(0x05),
        IS_FIRE(0x06),
        IS_TECHNO_TYPE_1(0x07),
        IS_TECHNO_TYPE_2(0x08),
        IS_TECHNO_TYPE_3(0x09),
        IS_PRE_ALARM(0x0a),
        IS_PANIC(0x0b),
        IS_SILENT_PANIC(0x0c),
        IS_HOLD_UP(0x0d),
        IS_FIRE_RESET(0x0e),
        IS_EMERGENCY(0x0f),
        IS_KEY(0x10),
        IS_TAMPERING(0x11),
        IS_FAULT(0x12),
        IS_INPUT_TEST(0x13);

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

    // Solo per IC_SHOCK e IC_ROLLER
    public enum Sensitivity {
        IS_LOW(0x80),
        IS_MEDIUM(0x40),
        IS_HIGH(0x00);

        private byte value;

        Sensitivity(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Sensitivity valueOf(byte value) {
            Sensitivity   retval = null;

            for (Sensitivity pivot : Sensitivity.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    // bitmask!
    public enum Flags {
        IF_NONE(0x00),
        IF_EXCLUSION_ENABLED(0x01),
        IF_DOUBLE_RELEASE(0x02),
        IF_OR_SECTORS(0x08),
        IF_ALL(0x0b);

        private byte value;

        Flags(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Flags valueOf(byte value) {
            Flags   retval = null;

            for (Flags pivot : Flags.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        public static boolean is(byte value, Flags flag) {
            return (value & flag.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((IF_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    public enum Functions {
        IF_NO_MOVEMENT(0x01),
        IF_GONG(0x02),
        IF_COURTESY_LIGHT(0x04),
        IF_OPEN_DOOR(0x08);

        private byte value;

        Functions(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Functions valueOf(byte value) {
            Functions   retval = null;

            for (Functions pivot : Functions.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum Delay {
        ID_5_SECS(0x00),
        ID_10_SECS(0x01),
        ID_30_SECS(0x02),
        ID_60_SECS(0x03),
        ID_90_SECS(0x04),
        ID_5_MINS(0x05),
        ID_20_SECS(0x06);

        private byte value;

        Delay(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Delay valueOf(byte value) {
            Delay   retval = null;

            for (Delay pivot : Delay.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum Video {
        IV_NONE(0x00),
        IV_CAMERA_1(0x10),
        IV_CAMERA_2(0x20),
        IV_CAMERA_3(0x40),
        IV_CAMERA_4(0x80);

        private byte value;

        Video(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Video valueOf(byte value) {
            Video   retval = null;

            for (Video pivot : Video.values()) {
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
    private Sensitivity     sensitivity;
    private byte            flags;
    private Video           video;
    private boolean[]       associatedPartitions;
    private String          name;
    private Delay           delay;

    public Input(int logicNumber, Configuration configuration, Specialization specialization, Sensitivity sensitivity, byte flags, Video video, boolean[] associatedPartitions, String name, Delay delay)
    {
        setLogicNumber(logicNumber);
        setConfiguration(configuration);
        setSpecialization(specialization);
        setSensitivity(sensitivity);
        setFlags(flags);
        setVideo(video);
        setAssociatedPartitions(associatedPartitions);
        setName(name);
        setDelay(delay);
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

    public Sensitivity getSensitivity()
    {
        return sensitivity;
    }

    public void setSensitivity(Sensitivity sensitivity)
    {
        if (sensitivity == null) sensitivity = Sensitivity.IS_HIGH;

        this.sensitivity = sensitivity;
    }

    public byte getFlags()
    {
        return flags;
    }

    public void setFlags(byte flags)
    {
        if (!Flags.isValid(flags)) throw new IllegalArgumentException(String.format("Invalid flags bitmap 0x%02x", flags));

        this.flags = flags;
    }

    public Video getVideo()
    {
        return video;
    }

    public void setVideo(Video video)
    {
        if (video == null) video = Video.IV_NONE;

        this.video = video;
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

    public Delay getDelay()
    {
        return delay;
    }

    public void setDelay(Delay delay)
    {
        if (delay == null) delay = Delay.ID_5_SECS;

        this.delay = delay;
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
