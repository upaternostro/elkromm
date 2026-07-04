package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class ParametersEnablings implements Serializable {
    public enum Time {
        PET_30_SECS(0x00),
        PET_60_SECS(0x01),
        PET_90_SECS(0x02),
        PET_180_SECS(0x03),
        PET_9_MINS(0x04);

        private byte    value;

        Time(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static Time valueOf(byte value) {
            Time    retval = null;

            for (Time pivot : Time.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum AlarmCount {
        PEAC_NO_COUNT(0x00),
        PEAC_TWO(0x01),
        PEAC_FOUR(0x02),
        PEAC_SIX(0x03),
        PEAC_EIGHT(0x04);

        private byte    value;

        AlarmCount(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static AlarmCount valueOf(byte value) {
            AlarmCount    retval = null;

            for (AlarmCount pivot : AlarmCount.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum PowerLack {
        PEPL_1_HOUR(0x00),
        PEPL_2_HOUR(0x01),
        PEPL_4_HOUR(0x02);

        private byte    value;

        PowerLack(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static PowerLack valueOf(byte value) {
            PowerLack    retval = null;

            for (PowerLack pivot : PowerLack.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    // bitmask!
    public enum Play {
        PEP_NONE(0x00),
        PEP_FAULT(0x01),
        PEP_SECTS(0x02),
        PEP_SYSTEM(0x04),
        PEP_SERVICE(0x08),
        PEP_ALL(0x0f);

        private byte value;

        Play(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Play valueOf(byte value) {
            Play   retval = null;

            for (Play pivot : Play.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        public static boolean is(byte value, Play play) {
            return (value & play.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((PEP_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    // bitmask!
    public enum Help {
        PEH_NONE(0x00),
        PEH_ENABLE(0x80),
        PEH_KMASK(0x07),
        PEH_ALL(0x87);

        private byte value;

        Help(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Help valueOf(byte value) {
            Help   retval = null;

            for (Help pivot : Help.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        public static boolean is(byte value, Help help) {
            return (value & help.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((PEH_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    public enum Enabling {
        PEE_DISABLE(0x00),
        PEE_ENABLE(0x01);

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

    public enum Notice {
        PEN_NO_NOTICE(0x00),
        PEN_5_MINS(0x05),
        PEN_10_MINS(0x0a),
        PEN_15_MINS(0x0f),
        PEN_20_MINS(0x14);

        private byte    value;

        Notice(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static Notice valueOf(byte value) {
            Notice    retval = null;

            for (Notice pivot : Notice.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    // bitmask!
    public enum DST {
        PED_NONE(0x00),
        PED_DISABLE(0x00),
        PED_ENABLE(0x01),
        PED_FIRST_SUNDAY(0x00),
        PED_LAST_SUNDAY(0x02),
        PED_ALL(0x03);

        private byte value;

        DST(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static DST valueOf(byte value) {
            DST   retval = null;

            for (DST pivot : DST.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }

        public static boolean is(byte value, DST help) {
            return (value & help.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((PED_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    public enum Month {
        PEM_JANUARY(0x01),
        PEM_FEBRUARY(0x02),
        PEM_MARCH(0x03),
        PEM_APRIL(0x04),
        PEM_MAY(0x05),
        PEM_JUNE(0x06),
        PEM_JULY(0x07),
        PEM_AUGUST(0x08),
        PEM_SEPTEMBER(0x09),
        PEM_OCTOBER(0x0a),
        PEM_NOVEMBER(0x0b),
        PEM_DECEMBER(0x0c);

        private byte value;

        Month(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Month valueOf(byte value) {
            Month   retval = null;

            for (Month pivot : Month.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private Time        bulgarTime;
    private Time        emergencyTime;
    private Time        preAlarmTime;
    private AlarmCount  alarmCount;
    private PowerLack   powerLack;
    private byte        play;
    private byte        help;
    private Enabling    lan;
    private Enabling    timeProgrammer;
    private Notice      notice;
    private byte        dST;
    private Month       on;
    private Month       off;

    public ParametersEnablings(Time bulgarTime, Time emergencyTime, Time preAlarmTime, AlarmCount alarmCount, PowerLack powerLack, byte play, byte help, Enabling lan, Enabling timeProgrammer, Notice notice, byte dST, Month on, Month off) {
        setBulgarTime(bulgarTime);
        setEmergencyTime(emergencyTime);
        setPreAlarmTime(preAlarmTime);
        setAlarmCount(alarmCount);
        setPowerLack(powerLack);
        setPlay(play);
        setHelp(help);
        setLan(lan);
        setTimeProgrammer(timeProgrammer);
        setNotice(notice);
        setDST(dST);
        setOn(on);
        setOff(off);
    }

    public Time getBulgarTime() {
        return bulgarTime;
    }

    public void setBulgarTime(Time bulgarTime) {
        if (bulgarTime == null) throw new IllegalArgumentException("Missing mandatory bulgarTime");

        this.bulgarTime = bulgarTime;
    }

    public Time getEmergencyTime() {
        return emergencyTime;
    }

    public void setEmergencyTime(Time emergencyTime) {
        if (emergencyTime == null) throw new IllegalArgumentException("Missing mandatory emergencyTime");
        
        this.emergencyTime = emergencyTime;
    }

    public Time getPreAlarmTime() {
        return preAlarmTime;
    }

    public void setPreAlarmTime(Time preAlarmTime) {
        if (preAlarmTime == null) throw new IllegalArgumentException("Missing mandatory preAlarmTime");
        
        this.preAlarmTime = preAlarmTime;
    }

    public AlarmCount getAlarmCount() {
        return alarmCount;
    }

    public void setAlarmCount(AlarmCount alarmCount) {
        if (alarmCount == null) throw new IllegalArgumentException("Missing mandatory alarmCount");
        
        this.alarmCount = alarmCount;
    }

    public PowerLack getPowerLack() {
        return powerLack;
    }

    public void setPowerLack(PowerLack powerLack) {
        if (powerLack == null) throw new IllegalArgumentException("Missing mandatory powerLack");
        
        this.powerLack = powerLack;
    }

    public byte getPlay() {
        return play;
    }

    public void setPlay(byte play) {
        if (!Play.isValid(play)) throw new IllegalArgumentException("Wrong value play");

        this.play = play;
    }

    public byte getHelp() {
        return help;
    }

    public void setHelp(byte help) {
        if (!Help.isValid(help)) throw new IllegalArgumentException("Wrong value help");
        
        this.help = help;
    }

    public Enabling getLan() {
        return lan;
    }

    public void setLan(Enabling lan) {
        if (lan == null) throw new IllegalArgumentException("Missing mandatory lan");
        
        this.lan = lan;
    }

    public Enabling getTimeProgrammer() {
        return timeProgrammer;
    }

    public void setTimeProgrammer(Enabling timeProgrammer) {
        if (timeProgrammer == null) throw new IllegalArgumentException("Missing mandatory timeProgrammer");
        
        this.timeProgrammer = timeProgrammer;
    }

    public Notice getNotice() {
        return notice;
    }

    public void setNotice(Notice notice) {
        if (notice == null) throw new IllegalArgumentException("Missing mandatory notice");
        
        this.notice = notice;
    }

    public byte getDST() {
        return dST;
    }

    public void setDST(byte dST) {
        if (!DST.isValid(dST)) throw new IllegalArgumentException("Wrong value dST");
        
        this.dST = dST;
    }

    public Month getOn() {
        return on;
    }

    public void setOn(Month on) {
        if (on == null) throw new IllegalArgumentException("Missing mandatory on");
        
        this.on = on;
    }

    public Month getOff() {
        return off;
    }

    public void setOff(Month off) {
        if (off == null) throw new IllegalArgumentException("Missing mandatory off");
        
        this.off = off;
    }
}
