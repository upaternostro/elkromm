package org.paternostro.elkromm.dto;

import java.io.Serializable;

/**
 * General system parameters and enablings: alarm timing, alarm count,
 * power-lack timeout, keypad "play" and help behavior, LAN/time-programmer
 * enabling, notice timeout, and daylight-saving-time schedule.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ParametersEnablings implements Serializable {
    /** A timeout duration, used for several timing fields of this class. */
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

        /**
         * Returns the raw byte value of this timing.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code Time} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching timing, or {@code null} if none matches
         */
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

    /** How many consecutive alarms are counted before some restriction kicks in. */
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

        /**
         * Returns the raw byte value of this count.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code AlarmCount} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching count, or {@code null} if none matches
         */
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

    /** How long the panel waits, after a mains power lack, before reporting it. */
    public enum PowerLack {
        PEPL_1_HOUR(0x00),
        PEPL_2_HOUR(0x01),
        PEPL_4_HOUR(0x02);

        private byte    value;

        PowerLack(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this timeout.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code PowerLack} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching timeout, or {@code null} if none matches
         */
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

    /**
     * Bitmask of which system conditions are announced by voice at keypads
     * ("play"). {@code PEP_NONE} and {@code PEP_ALL} are convenience
     * aliases, not real single-bit values.
     */
    // bitmask!
    public enum Play {
        PEP_NONE(0x00),
        /** Announce faults. */
        PEP_FAULT(0x01),
        /** Announce partition/sector status. */
        PEP_SECTS(0x02),
        /** Announce system status. */
        PEP_SYSTEM(0x04),
        /** Announce service/maintenance conditions. */
        PEP_SERVICE(0x08),
        PEP_ALL(0x0f);

        private byte value;

        Play(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this play condition.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Play} matching an exact raw bitmask value.
         *
         * @param value the raw value to look up
         * @return the matching condition, or {@code null} if none matches
         */
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

        /**
         * Checks whether a given condition's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param play the single condition bit to look for
         * @return {@code true} if {@code play}'s bit is set in {@code value}
         */
        public static boolean is(byte value, Play play) {
            return (value & play.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid play bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #PEP_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((PEP_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /**
     * Bitmask controlling the keypad help message: enabling flag packed
     * together with a keypad address nibble.
     * {@code PEH_NONE} and {@code PEH_ALL} are convenience aliases, not real
     * single-bit values.
     */
    // bitmask!
    public enum Help {
        PEH_NONE(0x00),
        /** Help message enabled. */
        PEH_ENABLE(0x80),
        /** Mask for the low nibble, encoding (keypad address - 1). */
        PEH_KMASK(0x07),
        PEH_ALL(0x87);

        private byte value;

        Help(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this help flag/mask.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Help} matching an exact raw bitmask value.
         *
         * @param value the raw value to look up
         * @return the matching flag/mask, or {@code null} if none matches
         */
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

        /**
         * Checks whether a given flag's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param help the single flag bit to look for
         * @return {@code true} if {@code help}'s bit is set in {@code value}
         */
        public static boolean is(byte value, Help help) {
            return (value & help.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid help bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #PEH_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((PEH_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /** Simple enabled/disabled flag, used by several fields of this class. */
    public enum Enabling {
        PEE_DISABLE(0x00),
        PEE_ENABLE(0x01);

        private byte    value;

        Enabling(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this flag.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code Enabling} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching flag, or {@code null} if none matches
         */
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

    /** How long before an arming/disarming notice call is placed. */
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

        /**
         * Returns the raw byte value of this timeout.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code Notice} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching timeout, or {@code null} if none matches
         */
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

    /**
     * Bitmask controlling automatic daylight-saving-time adjustment.
     * <p>
     * Several constants intentionally share the value {@code 0x00}
     * ({@code PED_NONE}, {@code PED_DISABLE}, {@code PED_FIRST_SUNDAY}):
     * bit 0 controls enable/disable, bit 1 controls which Sunday of the
     * month is used, and {@code 0x00} is simultaneously "disabled" and
     * "first Sunday" until bit 0 is set.
     */
    // bitmask!
    public enum DST {
        PED_NONE(0x00),
        /** DST auto-adjustment disabled. */
        PED_DISABLE(0x00),
        /** DST auto-adjustment enabled. */
        PED_ENABLE(0x01),
        /** Adjust on the first Sunday of the month. */
        PED_FIRST_SUNDAY(0x00),
        /** Adjust on the last Sunday of the month. */
        PED_LAST_SUNDAY(0x02),
        PED_ALL(0x03);

        private byte value;

        DST(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw bitmask value of this DST setting.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code DST} matching an exact raw bitmask value.
         * <p>
         * Since several constants share the same value, this returns
         * whichever is declared first ({@code PED_NONE}/{@code PED_DISABLE}/{@code PED_FIRST_SUNDAY} for {@code 0x00}).
         *
         * @param value the raw value to look up
         * @return the matching setting, or {@code null} if none matches
         */
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

        /**
         * Checks whether a given setting's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param help the single setting bit to look for
         * @return {@code true} if {@code help}'s bit is set in {@code value}
         */
        public static boolean is(byte value, DST help) {
            return (value & help.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid DST bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #PED_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((PED_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /** A calendar month, used for the DST on/off schedule. */
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

        /**
         * Returns the raw byte value of this month.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Month} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching month, or {@code null} if none matches
         */
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

    /**
     * Creates a new parameters/enablings configuration.
     *
     * @param bulgarTime burglary alarm duration
     * @param emergencyTime emergency alarm duration
     * @param preAlarmTime pre-alarm duration
     * @param alarmCount how many consecutive alarms are counted
     * @param powerLack mains power lack reporting timeout
     * @param play a valid {@link Play} bitmask
     * @param help a valid {@link Help} bitmask
     * @param lan whether the LAN interface is enabled
     * @param timeProgrammer whether the time programmer is enabled
     * @param notice notice call timeout
     * @param dST a valid {@link DST} bitmask
     * @param on month DST adjustment turns on
     * @param off month DST adjustment turns off
     */
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

    /**
     * Returns the burglary alarm duration.
     *
     * @return the duration
     */
    public Time getBulgarTime() {
        return bulgarTime;
    }

    /**
     * Sets the burglary alarm duration.
     *
     * @param bulgarTime the duration to set, not {@code null}
     * @throws IllegalArgumentException if {@code bulgarTime} is {@code null}
     */
    public void setBulgarTime(Time bulgarTime) {
        if (bulgarTime == null) throw new IllegalArgumentException("Missing mandatory bulgarTime");

        this.bulgarTime = bulgarTime;
    }

    /**
     * Returns the emergency alarm duration.
     *
     * @return the duration
     */
    public Time getEmergencyTime() {
        return emergencyTime;
    }

    /**
     * Sets the emergency alarm duration.
     *
     * @param emergencyTime the duration to set, not {@code null}
     * @throws IllegalArgumentException if {@code emergencyTime} is {@code null}
     */
    public void setEmergencyTime(Time emergencyTime) {
        if (emergencyTime == null) throw new IllegalArgumentException("Missing mandatory emergencyTime");
        
        this.emergencyTime = emergencyTime;
    }

    /**
     * Returns the pre-alarm duration.
     *
     * @return the duration
     */
    public Time getPreAlarmTime() {
        return preAlarmTime;
    }

    /**
     * Sets the pre-alarm duration.
     *
     * @param preAlarmTime the duration to set, not {@code null}
     * @throws IllegalArgumentException if {@code preAlarmTime} is {@code null}
     */
    public void setPreAlarmTime(Time preAlarmTime) {
        if (preAlarmTime == null) throw new IllegalArgumentException("Missing mandatory preAlarmTime");
        
        this.preAlarmTime = preAlarmTime;
    }

    /**
     * Returns how many consecutive alarms are counted.
     *
     * @return the alarm count
     */
    public AlarmCount getAlarmCount() {
        return alarmCount;
    }

    /**
     * Sets how many consecutive alarms are counted.
     *
     * @param alarmCount the count to set, not {@code null}
     * @throws IllegalArgumentException if {@code alarmCount} is {@code null}
     */
    public void setAlarmCount(AlarmCount alarmCount) {
        if (alarmCount == null) throw new IllegalArgumentException("Missing mandatory alarmCount");
        
        this.alarmCount = alarmCount;
    }

    /**
     * Returns the mains power lack reporting timeout.
     *
     * @return the timeout
     */
    public PowerLack getPowerLack() {
        return powerLack;
    }

    /**
     * Sets the mains power lack reporting timeout.
     *
     * @param powerLack the timeout to set, not {@code null}
     * @throws IllegalArgumentException if {@code powerLack} is {@code null}
     */
    public void setPowerLack(PowerLack powerLack) {
        if (powerLack == null) throw new IllegalArgumentException("Missing mandatory powerLack");
        
        this.powerLack = powerLack;
    }

    /**
     * Returns the raw "play" (voice announcement) bitmask.
     *
     * @return the {@link Play} bitmask
     */
    public byte getPlay() {
        return play;
    }

    /**
     * Sets the "play" (voice announcement) bitmask.
     *
     * @param play a valid {@link Play} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setPlay(byte play) {
        if (!Play.isValid(play)) throw new IllegalArgumentException("Wrong value play");

        this.play = play;
    }

    /**
     * Returns the raw keypad help message bitmask.
     *
     * @return the {@link Help} bitmask
     */
    public byte getHelp() {
        return help;
    }

    /**
     * Sets the keypad help message bitmask.
     *
     * @param help a valid {@link Help} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setHelp(byte help) {
        if (!Help.isValid(help)) throw new IllegalArgumentException("Wrong value help");
        
        this.help = help;
    }

    /**
     * Returns whether the LAN interface is enabled.
     *
     * @return the flag
     */
    public Enabling getLan() {
        return lan;
    }

    /**
     * Sets whether the LAN interface is enabled.
     *
     * @param lan the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code lan} is {@code null}
     */
    public void setLan(Enabling lan) {
        if (lan == null) throw new IllegalArgumentException("Missing mandatory lan");
        
        this.lan = lan;
    }

    /**
     * Returns whether the time programmer is enabled.
     *
     * @return the flag
     */
    public Enabling getTimeProgrammer() {
        return timeProgrammer;
    }

    /**
     * Sets whether the time programmer is enabled.
     *
     * @param timeProgrammer the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code timeProgrammer} is {@code null}
     */
    public void setTimeProgrammer(Enabling timeProgrammer) {
        if (timeProgrammer == null) throw new IllegalArgumentException("Missing mandatory timeProgrammer");
        
        this.timeProgrammer = timeProgrammer;
    }

    /**
     * Returns the notice call timeout.
     *
     * @return the timeout
     */
    public Notice getNotice() {
        return notice;
    }

    /**
     * Sets the notice call timeout.
     *
     * @param notice the timeout to set, not {@code null}
     * @throws IllegalArgumentException if {@code notice} is {@code null}
     */
    public void setNotice(Notice notice) {
        if (notice == null) throw new IllegalArgumentException("Missing mandatory notice");
        
        this.notice = notice;
    }

    /**
     * Returns the raw daylight-saving-time bitmask.
     *
     * @return the {@link DST} bitmask
     */
    public byte getDST() {
        return dST;
    }

    /**
     * Sets the daylight-saving-time bitmask.
     *
     * @param dST a valid {@link DST} bitmask
     * @throws IllegalArgumentException if not a valid bitmask
     */
    public void setDST(byte dST) {
        if (!DST.isValid(dST)) throw new IllegalArgumentException("Wrong value dST");
        
        this.dST = dST;
    }

    /**
     * Returns the month DST adjustment turns on.
     *
     * @return the month
     */
    public Month getOn() {
        return on;
    }

    /**
     * Sets the month DST adjustment turns on.
     *
     * @param on the month to set, not {@code null}
     * @throws IllegalArgumentException if {@code on} is {@code null}
     */
    public void setOn(Month on) {
        if (on == null) throw new IllegalArgumentException("Missing mandatory on");
        
        this.on = on;
    }

    /**
     * Returns the month DST adjustment turns off.
     *
     * @return the month
     */
    public Month getOff() {
        return off;
    }

    /**
     * Sets the month DST adjustment turns off.
     *
     * @param off the month to set, not {@code null}
     * @throws IllegalArgumentException if {@code off} is {@code null}
     */
    public void setOff(Month off) {
        if (off == null) throw new IllegalArgumentException("Missing mandatory off");
        
        this.off = off;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{bulgarTime=").append(bulgarTime).append(", emergencyTime=").append(emergencyTime).append(", preAlarmTime="
               ).append(preAlarmTime).append(", alarmCount=").append(alarmCount).append(", powerLack=").append(powerLack).append(", play=").append(play
               ).append(", help=").append(help).append(", lan=").append(lan).append(", timeProgrammer=").append(timeProgrammer).append(", notice=").append(notice
               ).append(", dST=").append(dST).append(", on=").append(on).append(", off=").append(off).append("}");
        
        return sb.toString();
    }
}
