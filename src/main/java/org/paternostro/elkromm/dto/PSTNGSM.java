package org.paternostro.elkromm.dto;

import java.io.Serializable;

/**
 * PSTN/GSM line configuration: landline and mobile network enabling,
 * country-specific dialing parameters, line supervision, and GSM SIM
 * details.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PSTNGSM implements Serializable {
    /** Simple enabled/disabled flag, used by several fields of this class. */
    public enum Enabling {
        PGE_DISABLED(0x00),
        PGE_ENABLED(0x01);

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

    /** Country-specific PSTN dialing rules to apply. */
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

        /**
         * Returns the raw byte value of this country.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code Country} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching country, or {@code null} if none matches
         */
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

    /** Digit dialed to get an outside line through a PABX, if any. */
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
        /** No PABX access digit needed. */
        PGPLAD_DISABLE(0xff);

        private byte    value;

        PABXLocalAccessDigit(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this digit.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code PABXLocalAccessDigit} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching digit, or {@code null} if none matches
         */
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

    /** How often the panel tests the PSTN line for continuity. */
    public enum PSTNLineTestFrequency {
        /** No line test. */
        PGPLTF_DISABLE(0x00),
        /** Every 24 hours. */
        PGPLTF_24_HOURS(0x01),
        /** Every time the system is armed. */
        PGPLTF_SYSTEM_ON(0x02);

        private byte    value;

        PSTNLineTestFrequency(int value) {
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
         * Looks up the {@code PSTNLineTestFrequency} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching frequency, or {@code null} if none matches
         */
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

    /** How many rings the panel waits before answering, when an answering machine may also be on the line. */
    public enum PSTNAnsweringMachineRings {
        PGPAMR_DISABLE(0x00),
        PGPAMR_2_RINGS(0x02),
        PGPAMR_4_RINGS(0x04),
        PGPAMR_8_RINGS(0x08);

        private byte    value;

        PSTNAnsweringMachineRings(int value) {
            this.value = (byte)(value & 0xFF);
        }

        /**
         * Returns the raw byte value of this rings count.
         *
         * @return the raw value
         */
        public byte getValue() {
            return value;
        }

        /**
         * Looks up the {@code PSTNAnsweringMachineRings} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching rings count, or {@code null} if none matches
         */
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

    /**
     * Creates a new PSTN/GSM configuration.
     *
     * @param enablePSTN whether the PSTN network is enabled
     * @param country dialing rules to apply
     * @param pABXLocalAccessDigit PABX outside-line access digit, if any
     * @param toneControl whether dial-tone detection is enabled
     * @param answerControl whether call-answered detection is enabled
     * @param pSTNLineTestFrequency how often to test the PSTN line
     * @param pSTNAnsweringMachineRings rings to wait before answering on PSTN
     * @param enableGSM whether the GSM network is enabled
     * @param enableGSMAnsweringMachine whether GSM answering-machine mode is enabled
     * @param enableIncomingSMS whether incoming SMS commands are accepted
     * @param gSMPin the GSM SIM PIN, or {@code -1} if none
     * @param expirationMonth SIM expiration month, in range [1, 12]
     * @param expirationYear SIM expiration year (2 digits), in range [0, 99]
     */
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

    /**
     * Returns whether the PSTN network is enabled.
     *
     * @return the PSTN enabling flag
     */
    public Enabling getEnablePSTN() {
        return enablePSTN;
    }

    /**
     * Sets whether the PSTN network is enabled.
     *
     * @param enablePSTN the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code enablePSTN} is {@code null}
     */
    public void setEnablePSTN(Enabling enablePSTN) {
        if (enablePSTN == null) throw new IllegalArgumentException("Missing mandatory enablePSTN");

        this.enablePSTN = enablePSTN;
    }

    /**
     * Returns the dialing rules country.
     *
     * @return the country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the dialing rules country.
     *
     * @param country the country to set, not {@code null}
     * @throws IllegalArgumentException if {@code country} is {@code null}
     */
    public void setCountry(Country country) {
        if (country == null) throw new IllegalArgumentException("Missing mandatory country");
        
        this.country = country;
    }

    /**
     * Returns the PABX outside-line access digit.
     *
     * @return the access digit
     */
    public PABXLocalAccessDigit getPABXLocalAccessDigit() {
        return pABXLocalAccessDigit;
    }

    /**
     * Sets the PABX outside-line access digit.
     *
     * @param pABXLocalAccessDigit the digit to set, not {@code null}
     * @throws IllegalArgumentException if {@code pABXLocalAccessDigit} is {@code null}
     */
    public void setPABXLocalAccessDigit(PABXLocalAccessDigit pABXLocalAccessDigit) {
        if (pABXLocalAccessDigit == null) throw new IllegalArgumentException("Missing mandatory pABXLocalAccessDigit");
        
        this.pABXLocalAccessDigit = pABXLocalAccessDigit;
    }

    /**
     * Returns whether dial-tone detection is enabled.
     *
     * @return the tone control flag
     */
    public Enabling getToneControl() {
        return toneControl;
    }

    /**
     * Sets whether dial-tone detection is enabled.
     *
     * @param toneControl the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code toneControl} is {@code null}
     */
    public void setToneControl(Enabling toneControl) {
        if (toneControl == null) throw new IllegalArgumentException("Missing mandatory toneControl");
        
        this.toneControl = toneControl;
    }

    /**
     * Returns whether call-answered detection is enabled.
     *
     * @return the answer control flag
     */
    public Enabling getAnswerControl() {
        return answerControl;
    }

    /**
     * Sets whether call-answered detection is enabled.
     *
     * @param answerControl the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code answerControl} is {@code null}
     */
    public void setAnswerControl(Enabling answerControl) {
        if (answerControl == null) throw new IllegalArgumentException("Missing mandatory answerControl");
        
        this.answerControl = answerControl;
    }

    /**
     * Returns how often the PSTN line is tested.
     *
     * @return the test frequency
     */
    public PSTNLineTestFrequency getPSTNLineTestFrequency() {
        return pSTNLineTestFrequency;
    }

    /**
     * Sets how often the PSTN line is tested.
     *
     * @param pSTNLineTestFrequency the frequency to set, not {@code null}
     * @throws IllegalArgumentException if {@code pSTNLineTestFrequency} is {@code null}
     */
    public void setPSTNLineTestFrequency(PSTNLineTestFrequency pSTNLineTestFrequency) {
        if (pSTNLineTestFrequency == null) throw new IllegalArgumentException("Missing mandatory pSTNLineTestFrequency");
        
        this.pSTNLineTestFrequency = pSTNLineTestFrequency;
    }

    /**
     * Returns how many rings are awaited before answering on PSTN.
     *
     * @return the rings count
     */
    public PSTNAnsweringMachineRings getPSTNAnsweringMachineRings() {
        return pSTNAnsweringMachineRings;
    }

    /**
     * Sets how many rings are awaited before answering on PSTN.
     *
     * @param pSTNAnsweringMachineRings the rings count to set, not {@code null}
     * @throws IllegalArgumentException if {@code pSTNAnsweringMachineRings} is {@code null}
     */
    public void setPSTNAnsweringMachineRings(PSTNAnsweringMachineRings pSTNAnsweringMachineRings) {
        if (pSTNAnsweringMachineRings == null) throw new IllegalArgumentException("Missing mandatory pSTNAnsweringMachineRings");

        this.pSTNAnsweringMachineRings = pSTNAnsweringMachineRings;
    }

    /**
     * Returns whether the GSM network is enabled.
     *
     * @return the GSM enabling flag
     */
    public Enabling getEnableGSM() {
        return enableGSM;
    }

    /**
     * Sets whether the GSM network is enabled.
     *
     * @param enableGSM the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code enableGSM} is {@code null}
     */
    public void setEnableGSM(Enabling enableGSM) {
        if (enableGSM == null) throw new IllegalArgumentException("Missing mandatory enableGSM");
        
        this.enableGSM = enableGSM;
    }

    /**
     * Returns whether GSM answering-machine mode is enabled.
     *
     * @return the flag
     */
    public Enabling getEnableGSMAnsweringMachine() {
        return enableGSMAnsweringMachine;
    }

    /**
     * Sets whether GSM answering-machine mode is enabled.
     *
     * @param enableGSMAnsweringMachine the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code enableGSMAnsweringMachine} is {@code null}
     */
    public void setEnableGSMAnsweringMachine(Enabling enableGSMAnsweringMachine) {
        if (enableGSMAnsweringMachine == null) throw new IllegalArgumentException("Missing mandatory enableGSMAnsweringMachine");
        
        this.enableGSMAnsweringMachine = enableGSMAnsweringMachine;
    }

    /**
     * Returns whether incoming SMS commands are accepted.
     *
     * @return the flag
     */
    public Enabling getEnableIncomingSMS() {
        return enableIncomingSMS;
    }

    /**
     * Sets whether incoming SMS commands are accepted.
     *
     * @param enableIncomingSMS the flag to set, not {@code null}
     * @throws IllegalArgumentException if {@code enableIncomingSMS} is {@code null}
     */
    public void setEnableIncomingSMS(Enabling enableIncomingSMS) {
        if (enableIncomingSMS == null) throw new IllegalArgumentException("Missing mandatory enableIncomingSMS");
        
        this.enableIncomingSMS = enableIncomingSMS;
    }

    /**
     * Returns the GSM SIM PIN.
     *
     * @return the PIN, or {@code -1} if none
     */
    public int getGSMPin() {
        return gSMPin;
    }

    /**
     * Sets the GSM SIM PIN.
     *
     * @param gSMPin the PIN to set, in range [-1, 999999]
     * @throws IllegalArgumentException if out of range
     */
    public void setGSMPin(int gSMPin) {
        if (gSMPin < -1 || gSMPin > 999999) throw new IllegalArgumentException("Wrong gSMPin, expected between -1 and 999999, found " + gSMPin);
        
        this.gSMPin = gSMPin;
    }

    /**
     * Returns the SIM expiration month.
     *
     * @return the month, in range [1, 12]
     */
    public byte getExpirationMonth() {
        return expirationMonth;
    }

    /**
     * Sets the SIM expiration month.
     *
     * @param expirationMonth the month to set, in range [1, 12]
     * @throws IllegalArgumentException if out of range
     */
    public void setExpirationMonth(byte expirationMonth) {
        if (expirationMonth < 1 || expirationMonth > 12) throw new IllegalArgumentException("Wrong expirationMonth, expected between 1 and 12, found " + expirationMonth);
        
        this.expirationMonth = expirationMonth;
    }

    /**
     * Returns the SIM expiration year.
     *
     * @return the 2-digit year, in range [0, 99]
     */
    public byte getExpirationYear() {
        return expirationYear;
    }

    /**
     * Sets the SIM expiration year.
     *
     * @param expirationYear the 2-digit year to set, in range [0, 99]
     * @throws IllegalArgumentException if out of range
     */
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
