package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class PSTNGSM implements Serializable {
    public enum Enabling {
        PGE_DISABLED(0x00),
        PGE_ENABLED(0x01);

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

    public enum Country {
        PGC_ITALY(0x00),
        PGC_FRANCE(0x01),
        PGC_GERMANY(0x02),
        PGC_CZECH_REPUBLIC(0x03),
        PGC_POLAND(0x04),
        PGC_SPAIN(0x05),
        PGC_PORTUGAL(0x06),
        PGC_GREECE(0x07),
        PGC_ENGLAND(0x08);

        private byte    value;

        Country(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static Country valueOf(byte value) {
            Country    retval = null;

            for (Country pivot : Country.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum PABXLocalAccessDigit {
        PGPLAD_0(0x00),
        PGPLAD_1(0x01),
        PGPLAD_2(0x02),
        PGPLAD_3(0x03),
        PGPLAD_4(0x04),
        PGPLAD_5(0x05),
        PGPLAD_6(0x06),
        PGPLAD_7(0x07),
        PGPLAD_8(0x08),
        PGPLAD_9(0x09),
        PGPLAD_DISABLE(0xff);

        private byte    value;

        PABXLocalAccessDigit(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static PABXLocalAccessDigit valueOf(byte value) {
            PABXLocalAccessDigit    retval = null;

            for (PABXLocalAccessDigit pivot : PABXLocalAccessDigit.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum PSTNLineTestFrequency {
        PGPLTF_DISABLE(0x00),
        PGPLTF_24_HOURS(0x01),
        PGPLTF_SYSTEM_ON(0x02);

        private byte    value;

        PSTNLineTestFrequency(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static PSTNLineTestFrequency valueOf(byte value) {
            PSTNLineTestFrequency    retval = null;

            for (PSTNLineTestFrequency pivot : PSTNLineTestFrequency.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    public enum PSTNAnsweringMachineRings {
        PGPAMR_DISABLE(0x00),
        PGPAMR_2_RINGS(0x02),
        PGPAMR_4_RINGS(0x04),
        PGPAMR_8_RINGS(0x08);

        private byte    value;

        PSTNAnsweringMachineRings(int value) {
            this.value = (byte)(value & 0xFF);
        }

        public byte getValue() {
            return value;
        }

        public static PSTNAnsweringMachineRings valueOf(byte value) {
            PSTNAnsweringMachineRings    retval = null;

            for (PSTNAnsweringMachineRings pivot : PSTNAnsweringMachineRings.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private Enabling                    enablePSTN;
    private Country                     country;
    private PABXLocalAccessDigit        pABXLocalAccessDigit;
    private Enabling                    toneControl;
    private Enabling                    answerControl;
    private PSTNLineTestFrequency       pSTNLineTestFrequency;
    private PSTNAnsweringMachineRings   pSTNAnsweringMachineRings;
    private Enabling                    enableGSM;
    private Enabling                    enableGSMAnsweringMachine;
    private Enabling                    enableIncomingSMS;
    private int                         gSMPin;
    private byte                        expirationMonth;
    private byte                        expirationYear;

    public PSTNGSM(Enabling enablePSTN, Country country, PABXLocalAccessDigit pABXLocalAccessDigit, Enabling toneControl, Enabling answerControl, PSTNLineTestFrequency pSTNLineTestFrequency, PSTNAnsweringMachineRings pSTNAnsweringMachineRings, Enabling enableGSM, Enabling enableGSMAnsweringMachine, Enabling enableIncomingSMS, int gSMPin, byte expirationMonth, byte expirationYear) {
        setEnablePSTN(enablePSTN);
        setCountry(country);
        setPABXLocalAccessDigit(pABXLocalAccessDigit);
        setToneControl(toneControl);
        setAnswerControl(answerControl);
        setPSTNLineTestFrequency(pSTNLineTestFrequency);
        setPSTNAnsweringMachineRings(pSTNAnsweringMachineRings);
        setEnableGSM(enableGSM);
        setEnableGSMAnsweringMachine(enableGSMAnsweringMachine);
        setEnableIncomingSMS(enableIncomingSMS);
        setGSMPin(gSMPin);
        setExpirationMonth(expirationMonth);
        setExpirationYear(expirationYear);
    }

    public Enabling getEnablePSTN() {
        return enablePSTN;
    }

    public void setEnablePSTN(Enabling enablePSTN) {
        if (enablePSTN == null) throw new IllegalArgumentException("Missing mandatory enablePSTN");

        this.enablePSTN = enablePSTN;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        if (country == null) throw new IllegalArgumentException("Missing mandatory country");
        
        this.country = country;
    }

    public PABXLocalAccessDigit getPABXLocalAccessDigit() {
        return pABXLocalAccessDigit;
    }

    public void setPABXLocalAccessDigit(PABXLocalAccessDigit pABXLocalAccessDigit) {
        if (pABXLocalAccessDigit == null) throw new IllegalArgumentException("Missing mandatory pABXLocalAccessDigit");
        
        this.pABXLocalAccessDigit = pABXLocalAccessDigit;
    }

    public Enabling getToneControl() {
        return toneControl;
    }

    public void setToneControl(Enabling toneControl) {
        if (toneControl == null) throw new IllegalArgumentException("Missing mandatory toneControl");
        
        this.toneControl = toneControl;
    }

    public Enabling getAnswerControl() {
        return answerControl;
    }

    public void setAnswerControl(Enabling answerControl) {
        if (answerControl == null) throw new IllegalArgumentException("Missing mandatory answerControl");
        
        this.answerControl = answerControl;
    }

    public PSTNLineTestFrequency getPSTNLineTestFrequency() {
        return pSTNLineTestFrequency;
    }

    public void setPSTNLineTestFrequency(PSTNLineTestFrequency pSTNLineTestFrequency) {
        if (pSTNLineTestFrequency == null) throw new IllegalArgumentException("Missing mandatory pSTNLineTestFrequency");
        
        this.pSTNLineTestFrequency = pSTNLineTestFrequency;
    }

    public PSTNAnsweringMachineRings getPSTNAnsweringMachineRings() {
        return pSTNAnsweringMachineRings;
    }

    public void setPSTNAnsweringMachineRings(PSTNAnsweringMachineRings pSTNAnsweringMachineRings) {
        if (pSTNAnsweringMachineRings == null) throw new IllegalArgumentException("Missing mandatory pSTNAnsweringMachineRings");

        this.pSTNAnsweringMachineRings = pSTNAnsweringMachineRings;
    }

    public Enabling getEnableGSM() {
        return enableGSM;
    }

    public void setEnableGSM(Enabling enableGSM) {
        if (enableGSM == null) throw new IllegalArgumentException("Missing mandatory enableGSM");
        
        this.enableGSM = enableGSM;
    }

    public Enabling getEnableGSMAnsweringMachine() {
        return enableGSMAnsweringMachine;
    }

    public void setEnableGSMAnsweringMachine(Enabling enableGSMAnsweringMachine) {
        if (enableGSMAnsweringMachine == null) throw new IllegalArgumentException("Missing mandatory enableGSMAnsweringMachine");
        
        this.enableGSMAnsweringMachine = enableGSMAnsweringMachine;
    }

    public Enabling getEnableIncomingSMS() {
        return enableIncomingSMS;
    }

    public void setEnableIncomingSMS(Enabling enableIncomingSMS) {
        if (enableGSMAnsweringMachine == null) throw new IllegalArgumentException("Missing mandatory enableGSMAnsweringMachine");
        
        this.enableIncomingSMS = enableIncomingSMS;
    }

    public int getGSMPin() {
        return gSMPin;
    }

    public void setGSMPin(int gSMPin) {
        if (gSMPin < -1 || gSMPin > 999999) throw new IllegalArgumentException("Wrong gSMPin, expected between -1 and 999999, found " + gSMPin);
        
        this.gSMPin = gSMPin;
    }

    public byte getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(byte expirationMonth) {
        if (expirationMonth < 1 || expirationMonth > 12) throw new IllegalArgumentException("Wrong expirationMonth, expected between 1 and 12, found " + expirationMonth);
        
        this.expirationMonth = expirationMonth;
    }

    public byte getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(byte expirationYear) {
        if (expirationYear < 0 || expirationYear > 99) throw new IllegalArgumentException("Wrong expirationYear, expected between 0 and 99, found " + expirationYear);
        
        this.expirationYear = expirationYear;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{enablePSTN=").
                append(enablePSTN).append(", country=").append(country).append(", pABXLocalAccessDigit="
               ).append(pABXLocalAccessDigit).append(", toneControl=").append(toneControl).append(", answerControl=").append(answerControl
               ).append(", pSTNLineTestFrequency=").append(pSTNLineTestFrequency).append(", pSTNAnsweringMachineRings="
               ).append(pSTNAnsweringMachineRings).append(", enableGSM=").append(enableGSM).append(", enableGSMAnsweringMachine="
               ).append(enableGSMAnsweringMachine).append(", enableIncomingSMS=").append(enableIncomingSMS).append(", gSMPin=").append(gSMPin
               ).append(", expirationMonth=").append(expirationMonth).append(", expirationYear=").append(expirationYear).append("}");
        
        return sb.toString();
    }
}
