package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class PhoneParameters implements Serializable {
    public enum Enabling {
        PPE_DISABLED(0x00),
        PPE_ENABLED(0x01);

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

    public enum ReturnCall {
        PPRC_DISABLED(0x00),
        PPRC_TYPE_A(0x01),
        PPRC_TYPE_B(0x02);

        private byte    value;

        ReturnCall(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

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

    public enum VoiceMessagesSendingMode {
        PPVMSM_NONE(0x00),
        PPVMSM_MODE_1(0x01),
        PPVMSM_MODE_2(0x02),
        PPVMSM_MODE_3(0x03),
        PPVMSM_MODE_4(0x04);

        private byte    value;

        VoiceMessagesSendingMode(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

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

    public enum CyclicTestCallFrequency {
        PPCTCF_DISABLE(0x00),
        PPCTCF_24_HOURS(0x01),
        PPCTCF_SYSTEM_ON(0x02);

        private byte    value;

        CyclicTestCallFrequency(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

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

        public byte getValue() {
            return value;
        }

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

    public Enabling getCallDelay() {
        return callDelay;
    }

    public void setCallDelay(Enabling callDelay) {
        if (callDelay == null) throw new IllegalArgumentException("Missing mandatory callDelay");

        this.callDelay = callDelay;
    }

    public ReturnCall getReturnCall() {
        return returnCall;
    }

    public void setReturnCall(ReturnCall returnCall) {
        if (returnCall == null) throw new IllegalArgumentException("Missing mandatory returnCall");
        
        this.returnCall = returnCall;
    }

    public Enabling getRemoteSurveillance() {
        return remoteSurveillance;
    }

    public void setRemoteSurveillance(Enabling remoteSurveillance) {
        if (remoteSurveillance == null) throw new IllegalArgumentException("Missing mandatory remoteSurveillance");
        
        this.remoteSurveillance = remoteSurveillance;
    }

    public VoiceMessagesSendingMode getVoiceMessagesSendingMode() {
        return voiceMessagesSendingMode;
    }

    public void setVoiceMessagesSendingMode(VoiceMessagesSendingMode voiceMessagesSendingMode) {
        if (voiceMessagesSendingMode == null) throw new IllegalArgumentException("Missing mandatory voiceMessagesSendingMode");
        
        this.voiceMessagesSendingMode = voiceMessagesSendingMode;
    }

    public CyclicTestCallFrequency getCyclicTestCallFrequency() {
        return cyclicTestCallFrequency;
    }

    public void setCyclicTestCallFrequency(CyclicTestCallFrequency cyclicTestCallFrequency) {
        if (cyclicTestCallFrequency == null) throw new IllegalArgumentException("Missing mandatory cyclicTestCallFrequency");
        
        this.cyclicTestCallFrequency = cyclicTestCallFrequency;
    }

    public byte getCyclicTestCallPhoneNumber() {
        return cyclicTestCallPhoneNumber;
    }

    public void setCyclicTestCallPhoneNumber(byte cyclicTestCallPhoneNumber) {
        if (cyclicTestCallPhoneNumber < 1 || cyclicTestCallPhoneNumber > 12) throw new IllegalArgumentException("Wrong cyclicTestCallPhoneNumber, expected between 1 and 12, found " + cyclicTestCallPhoneNumber);

        this.cyclicTestCallPhoneNumber = cyclicTestCallPhoneNumber;
    }

    public byte getCyclicTestCallHour() {
        return cyclicTestCallHour;
    }

    public void setCyclicTestCallHour(byte cyclicTestCallHour) {
        if (cyclicTestCallHour < 0 || cyclicTestCallHour > 23) throw new IllegalArgumentException("Wrong cyclicTestCallHour, expected between 0 and 23, found " + cyclicTestCallHour);
        
        this.cyclicTestCallHour = cyclicTestCallHour;
    }

    public byte getCyclicTestCallMinute() {
        return cyclicTestCallMinute;
    }

    public void setCyclicTestCallMinute(byte cyclicTestCallMinute) {
        if (cyclicTestCallMinute < 0 || cyclicTestCallMinute > 59) throw new IllegalArgumentException("Wrong cyclicTestCallMinute, expected between 0 and 59, found " + cyclicTestCallMinute);
        
        this.cyclicTestCallMinute = cyclicTestCallMinute;
    }

    public CyclicTestCallInterval getCyclicTestCallInterval() {
        return cyclicTestCallInterval;
    }

    public void setCyclicTestCallInterval(CyclicTestCallInterval cyclicTestCallInterval) {
        if (cyclicTestCallInterval == null) throw new IllegalArgumentException("Missing mandatory cyclicTestCallInterval");
        
        this.cyclicTestCallInterval = cyclicTestCallInterval;
    }
}
