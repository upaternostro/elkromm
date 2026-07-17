package org.paternostro.elkromm;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.DayClassCommands;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SingleKeyboard;
import org.paternostro.elkromm.dto.SingleSMS;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;

public interface ElkrommFacade
{
    enum Status {
        ST_NOT_INITIALIZED,
        ST_DISCONNECTED,
        ST_CONNECTED,
        ST_LOGGED_IN
    }

    public static final int MAX_AREAS = 4;
    public static final int MAX_PARTITIONS = 8;
    public static final int MAX_CREDENTIALS = 32;

    public static final int MAX_READERS = 16;
    public static final int MAX_EXPANSIONS = 7;
    public static final int MAX_KEYPADS = 8;
    
    public static final int MAX_LOGICAL_INPUTS = 64;
    public static final int MAX_EXP_INPUTS = 8;
    public static final int MAX_EXP_OUTPUTS = 6;

    public static final int MAX_PHONE_NUMBERS = 12;
    public static final int PHONE_NUMBER_LENGTH = 28; // digits!

    public static final int MAX_SMS = 9;

    public static final int MAX_DATA_LENGTH = 140;
    public static final int NAME_LENGTH = 24;
    public static final int SMS_LENGTH = 40;
    public static final int NUM_COMMANDS = 8;

    public static final byte BYTE_SOH = 0x01;
    public static final byte BYTE_ETX = 0x03;
    public static final byte BYTE_ACK = 0x06;
    public static final byte BYTE_DC1 = 0x11;
    public static final byte BYTE_NAK = 0x15;
    public static final byte BYTE_SYN = 0x16;

    // bitmask!
    enum Partition {
        P_NONE(0x00), // not a real bitmask
        P_ONE(0x01),
        P_TWO(0x02),
        P_THREE(0x04),
        P_FOUR(0x08),
        P_FIVE(0x10),
        P_SIX(0x20),
        P_SEVEN(0x40),
        P_EIGHT(0x80),
        P_ALL(0xFF); // not a real bitmask

        private byte value;

        Partition(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

        public static Partition valueOf(byte value)
        {
            for (Partition pivot : Partition.values()) {
                if (pivot.getValue() == value) {
                    return pivot;
                }
            }

            return null;
        }

        public static boolean is(byte value, Partition partition) {
            return (value & partition.getValue()) != 0;
        }

        public static boolean isValid(byte bitmask) {
            return (bitmask & ((P_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    enum InputStatus {
        IS_CLOSED(0x00), // WARNING: not a real bit mask! To select a closed input, search for a not open one ;)
        IS_OPEN(0x02),
        IS_ALARMED(0x04),
        IS_EXCLUDED(0x10),
        IS_ALL(0xff);   // hack to be always true ;)

        // FIXME: missing states: TAMPER, TAMPER_MEMORY, TEMPORARY_EXCLUDED

        private byte bitMask;

        InputStatus(int bitMask)
        {
            this.bitMask = (byte)bitMask;
        }

        public byte getBitMask()
        {
            return bitMask;
        }
    }

    void init(InetAddress inetAddr, int port, int plantCode);
    Status getStatus();
    void connect() throws ElkrommException;
    void disconnect() throws ElkrommException;
    void login(int plantCode, int technicalCode) throws ElkrommException;
    void ping() throws ElkrommException;
    AreasAndPartitions getAreasAndPartitions() throws ElkrommException;
    SystemStatus getSystemStatus() throws ElkrommException;
    void armDisarmSector(Partition sector, boolean arm) throws ElkrommException;
    Map<InputStatus,List<Integer>> getInputStatus() throws ElkrommException;
    void excludeIncludeInput(byte inputOrdinal, boolean exclude) throws ElkrommException;
    PeripheralUnits getPeripheralUnitsAddresses() throws ElkrommException;
    Checksums getChecksums() throws ElkrommException;
    Credential[] getUsers() throws ElkrommException;
    Credential[] getKeys() throws ElkrommException;
    Expansion[] getExpansions() throws ElkrommException;
    boolean[] getUserEnablings() throws ElkrommException;
    void enableDisableUser(byte userOrdinal, boolean enable) throws ElkrommException;
    void logout() throws ElkrommException;
    // v0.3 APIs
    Keyboard[] getKeyboards() throws ElkrommException;
    ParametersEnablings getParametersEnablings() throws ElkrommException;
    PhoneNumbersSendingCodes getPhoneNumbersSendingCodes() throws ElkrommException;
    PhoneParameters getPhoneParameters() throws ElkrommException;
    PSTNGSM getPSTNGSM() throws ElkrommException;
    Reader[] getReaders() throws ElkrommException;
    SMSs getSMSs() throws ElkrommException;
    C200bParameters getC200bParameters() throws ElkrommException;
    TimeProgrammer getTimeProgrammer() throws ElkrommException;
    void armDisarmSectors(byte partitions, byte armingMask) throws ElkrommException;
    // setters
    void setParametersEnablings(ParametersEnablings parametersEnablings) throws ElkrommException;
    void setAreasAndPartitions(AreasAndPartitions areasAndPartitions) throws ElkrommException;
    void setPhoneParameters(PhoneParameters phoneParameters) throws ElkrommException;
    void setPhoneNumbersSendingCodes(PhoneNumbersSendingCodes phoneNumbersSendingCodes) throws ElkrommException;
    void setC200bParameters(C200bParameters c200bParameters) throws ElkrommException;
    void setSMSs(SMSs sMSs) throws ElkrommException;
    void setPSTNGSM(PSTNGSM pSTNGSM) throws ElkrommException;
    void setUsers(Credential[] users) throws ElkrommException;
    void setKeys(Credential[] keys) throws ElkrommException;
    void setSMS(SingleSMS sMS) throws ElkrommException;
    void setKeyboard(SingleKeyboard keyboard) throws ElkrommException;
    void setReader(Reader reader) throws ElkrommException;
    void setDayClassCommands(DayClassCommands dayClassCommands) throws ElkrommException;
}
