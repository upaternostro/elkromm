package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * Telephone dialer parameters: call delay, return call, remote
 * surveillance, voice message mode, and cyclic test call scheduling.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneParameters implements Serializable {
    /** Simple enabled/disabled flag, used by several fields of this class. */
    public enum Enabling {
        PPE_DISABLED(0x00),
        PPE_ENABLED(0x01);

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

    /** Whether/how the panel calls back after being contacted, to reduce call costs. */
    public enum ReturnCall {
        /** No return call. */
        PPRC_DISABLED(0x00),
        /** Return call, first type. */
        PPRC_TYPE_A(0x01),
        /** Return call, second type. */
        PPRC_TYPE_B(0x02);

        private byte    value;

        ReturnCall(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this mode.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code ReturnCall} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching mode, or {@code null} if none matches
         */
        public static ReturnCall valueOf(byte value) {
            ReturnCall    retval = null;

            for (ReturnCall pivot : ReturnCall.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** How voice messages are sent over a phone call. */
    public enum VoiceMessagesSendingMode {
        /** No voice messages sent. */
        PPVMSM_NONE(0x00),
        /** Sending mode 1. */
        PPVMSM_MODE_1(0x01),
        /** Sending mode 2. */
        PPVMSM_MODE_2(0x02),
        /** Sending mode 3. */
        PPVMSM_MODE_3(0x03),
        /** Sending mode 4. */
        PPVMSM_MODE_4(0x04);

        private byte    value;

        VoiceMessagesSendingMode(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this mode.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code VoiceMessagesSendingMode} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching mode, or {@code null} if none matches
         */
        public static VoiceMessagesSendingMode valueOf(byte value) {
            VoiceMessagesSendingMode    retval = null;

            for (VoiceMessagesSendingMode pivot : VoiceMessagesSendingMode.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** How often the panel places an automatic test call. */
    public enum CyclicTestCallFrequency {
        /** No cyclic test call. */
        PPCTCF_DISABLE(0x00),
        /** Every 24 hours. */
        PPCTCF_24_HOURS(0x01),
        /** Every time the system is armed. */
        PPCTCF_SYSTEM_ON(0x02);

        private byte    value;

        CyclicTestCallFrequency(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this frequency.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code CyclicTestCallFrequency} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching frequency, or {@code null} if none matches
         */
        public static CyclicTestCallFrequency valueOf(byte value) {
            CyclicTestCallFrequency    retval = null;

            for (CyclicTestCallFrequency pivot : CyclicTestCallFrequency.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** Interval, in hours, between cyclic test calls when {@link CyclicTestCallFrequency#PPCTCF_24_HOURS} is not used. */
    public enum CyclicTestCallInterval {
        PPCTCI_1_HOUR(0x00),
        PPCTCI_4_HOURS(0x01),
        PPCTCI_8_HOURS(0x02),
        PPCTCI_12_HOURS(0x03),
        PPCTCI_24_HOURS(0x04),
        PPCTCI_48_HOURS(0x05),
        PPCTCI_72_HOURS(0x06),
        PPCTCI_96_HOURS(0x07),
        PPCTCI_120_HOURS(0x08),
        PPCTCI_144_HOURS(0x09),
        PPCTCI_168_HOURS(0x0a);

        private byte    value;

        CyclicTestCallInterval(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this interval.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code CyclicTestCallInterval} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching interval, or {@code null} if none matches
         */
        public static CyclicTestCallInterval valueOf(byte value) {
            CyclicTestCallInterval    retval = null;

            for (CyclicTestCallInterval pivot : CyclicTestCallInterval.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private Enabling                    callDelay;
    private ReturnCall                  returnCall;
    private Enabling                    remoteSurveillance;
    private VoiceMessagesSendingMode    voiceMessagesSendingMode;
    private CyclicTestCallFrequency     cyclicTestCallFrequency;
    private byte                        cyclicTestCallPhoneNumber;
    private byte                        cyclicTestCallHour;
    private byte                        cyclicTestCallMinute;
    private CyclicTestCallInterval      cyclicTestCallInterval;

    /**
     * Creates a new phone parameters configuration.
     *
     * @param callDelay whether a delay is applied before answering an incoming call
     * @param returnCall the return call mode
     * @param remoteSurveillance whether remote listen-in is enabled
     * @param voiceMessagesSendingMode how voice messages are sent
     * @param cyclicTestCallFrequency how often to place an automatic test call
     * @param cyclicTestCallPhoneNumber index of the phone number used for the test call, in range [0, {@link ElkrommFacade#MAX_PHONE_NUMBERS}]
     * @param cyclicTestCallHour hour of day for the test call, in range [0, 23]
     * @param cyclicTestCallMinute minute of the hour for the test call, in range [0, 59]
     * @param cyclicTestCallInterval interval between test calls
     */
    public PhoneParameters(Enabling callDelay, ReturnCall returnCall, Enabling remoteSurveillance, VoiceMessagesSendingMode voiceMessagesSendingMode, CyclicTestCallFrequency cyclicTestCallFrequency, byte cyclicTestCallPhoneNumber, byte cyclicTestCallHour, byte cyclicTestCallMinute, CyclicTestCallInterval cyclicTestCallInterval) {
        setCallDelay(callDelay);
        setReturnCall(returnCall);
        setRemoteSurveillance(remoteSurveillance);
        setVoiceMessagesSendingMode(voiceMessagesSendingMode);
        setCyclicTestCallFrequency(cyclicTestCallFrequency);
        setCyclicTestCallPhoneNumber(cyclicTestCallPhoneNumber);
        setCyclicTestCallHour(cyclicTestCallHour);
        setCyclicTestCallMinute(cyclicTestCallMinute);
        setCyclicTestCallInterval(cyclicTestCallInterval);
    }

    /**
     * Returns whether a delay is applied before answering an incoming call.
     *
     * @return the call delay flag
     */
    public Enabling getCallDelay() {
        return callDelay;
    }

    /**
     * Sets whether a delay is applied before answering an incoming call.
     *
     * @param callDelay the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code callDelay} is {@code null}
     */
    public void setCallDelay(Enabling callDelay) {
        if (callDelay == null) throw new IllegalArgumentException("Missing mandatory callDelay");

        this.callDelay = callDelay;
    }

    /**
     * Returns the return call mode.
     *
     * @return the return call mode
     */
    public ReturnCall getReturnCall() {
        return returnCall;
    }

    /**
     * Sets the return call mode.
     *
     * @param returnCall the mode to set, not {@code null}
     * @throws IllegalArgumentException if {@code returnCall} is {@code null}
     */
    public void setReturnCall(ReturnCall returnCall) {
        if (returnCall == null) throw new IllegalArgumentException("Missing mandatory returnCall");
        
        this.returnCall = returnCall;
    }

    /**
     * Returns whether remote listen-in is enabled.
     *
     * @return the remote surveillance flag
     */
    public Enabling getRemoteSurveillance() {
        return remoteSurveillance;
    }

    /**
     * Sets whether remote listen-in is enabled.
     *
     * @param remoteSurveillance the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code remoteSurveillance} is {@code null}
     */
    public void setRemoteSurveillance(Enabling remoteSurveillance) {
        if (remoteSurveillance == null) throw new IllegalArgumentException("Missing mandatory remoteSurveillance");
        
        this.remoteSurveillance = remoteSurveillance;
    }

    /**
     * Returns how voice messages are sent.
     *
     * @return the sending mode
     */
    public VoiceMessagesSendingMode getVoiceMessagesSendingMode() {
        return voiceMessagesSendingMode;
    }

    /**
     * Sets how voice messages are sent.
     *
     * @param voiceMessagesSendingMode the mode to set, not {@code null}
     * @throws IllegalArgumentException if {@code voiceMessagesSendingMode} is {@code null}
     */
    public void setVoiceMessagesSendingMode(VoiceMessagesSendingMode voiceMessagesSendingMode) {
        if (voiceMessagesSendingMode == null) throw new IllegalArgumentException("Missing mandatory voiceMessagesSendingMode");
        
        this.voiceMessagesSendingMode = voiceMessagesSendingMode;
    }

    /**
     * Returns how often the panel places an automatic test call.
     *
     * @return the test call frequency
     */
    public CyclicTestCallFrequency getCyclicTestCallFrequency() {
        return cyclicTestCallFrequency;
    }

    /**
     * Sets how often the panel places an automatic test call.
     *
     * @param cyclicTestCallFrequency the frequency to set, not {@code null}
     * @throws IllegalArgumentException if {@code cyclicTestCallFrequency} is {@code null}
     */
    public void setCyclicTestCallFrequency(CyclicTestCallFrequency cyclicTestCallFrequency) {
        if (cyclicTestCallFrequency == null) throw new IllegalArgumentException("Missing mandatory cyclicTestCallFrequency");
        
        this.cyclicTestCallFrequency = cyclicTestCallFrequency;
    }

    /**
     * Returns the index of the phone number used for the test call.
     *
     * @return the phone number index, in range [0, {@link ElkrommFacade#MAX_PHONE_NUMBERS}]
     */
    public byte getCyclicTestCallPhoneNumber() {
        return cyclicTestCallPhoneNumber;
    }

    /**
     * Sets the index of the phone number used for the test call.
     *
     * @param cyclicTestCallPhoneNumber the index to set, in range [0, {@link ElkrommFacade#MAX_PHONE_NUMBERS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setCyclicTestCallPhoneNumber(byte cyclicTestCallPhoneNumber) {
        if (cyclicTestCallPhoneNumber < 0 || cyclicTestCallPhoneNumber > ElkrommFacade.MAX_PHONE_NUMBERS) throw new IllegalArgumentException("Wrong cyclicTestCallPhoneNumber, expected between 0 and " + ElkrommFacade.MAX_PHONE_NUMBERS + ", found " + cyclicTestCallPhoneNumber);

        this.cyclicTestCallPhoneNumber = cyclicTestCallPhoneNumber;
    }

    /**
     * Returns the hour of day for the test call.
     *
     * @return the hour, in range [0, 23]
     */
    public byte getCyclicTestCallHour() {
        return cyclicTestCallHour;
    }

    /**
     * Sets the hour of day for the test call.
     *
     * @param cyclicTestCallHour the hour to set, in range [0, 23]
     * @throws IllegalArgumentException if out of range
     */
    public void setCyclicTestCallHour(byte cyclicTestCallHour) {
        if (cyclicTestCallHour < 0 || cyclicTestCallHour > 23) throw new IllegalArgumentException("Wrong cyclicTestCallHour, expected between 0 and 23, found " + cyclicTestCallHour);
        
        this.cyclicTestCallHour = cyclicTestCallHour;
    }

    /**
     * Returns the minute of the hour for the test call.
     *
     * @return the minute, in range [0, 59]
     */
    public byte getCyclicTestCallMinute() {
        return cyclicTestCallMinute;
    }

    /**
     * Sets the minute of the hour for the test call.
     *
     * @param cyclicTestCallMinute the minute to set, in range [0, 59]
     * @throws IllegalArgumentException if out of range
     */
    public void setCyclicTestCallMinute(byte cyclicTestCallMinute) {
        if (cyclicTestCallMinute < 0 || cyclicTestCallMinute > 59) throw new IllegalArgumentException("Wrong cyclicTestCallMinute, expected between 0 and 59, found " + cyclicTestCallMinute);
        
        this.cyclicTestCallMinute = cyclicTestCallMinute;
    }

    /**
     * Returns the interval between cyclic test calls.
     *
     * @return the interval
     */
    public CyclicTestCallInterval getCyclicTestCallInterval() {
        return cyclicTestCallInterval;
    }

    /**
     * Sets the interval between cyclic test calls.
     *
     * @param cyclicTestCallInterval the interval to set, not {@code null}
     * @throws IllegalArgumentException if {@code cyclicTestCallInterval} is {@code null}
     */
    public void setCyclicTestCallInterval(CyclicTestCallInterval cyclicTestCallInterval) {
        if (cyclicTestCallInterval == null) throw new IllegalArgumentException("Missing mandatory cyclicTestCallInterval");
        
        this.cyclicTestCallInterval = cyclicTestCallInterval;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{callDelay=").append(callDelay).append(", returnCall=").append(returnCall).append(", remoteSurveillance="
               ).append(remoteSurveillance).append(", voiceMessagesSendingMode=").append(voiceMessagesSendingMode
               ).append(", cyclicTestCallFrequency=").append(cyclicTestCallFrequency).append(", cyclicTestCallPhoneNumber="
               ).append(cyclicTestCallPhoneNumber).append(", cyclicTestCallHour=").append(cyclicTestCallHour).append(", cyclicTestCallMinute="
               ).append(cyclicTestCallMinute).append(", cyclicTestCallInterval=").append(cyclicTestCallInterval).append("}");
        
        return sb.toString();
    }
}
