package org.paternostro.elkromm;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMSs;
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
    void connect();
    void disconnect();
    void login(int plantCode, int technicalCode);
    void ping();
    AreasAndPartitions getAreasAndPartitions();
    SystemStatus getSystemStatus();
    void armDisarmSector(Partition sector, boolean arm);
    Map<InputStatus,List<Integer>> getInputStatus();
    void excludeIncludeInput(byte inputOrdinal, boolean exclude);
    PeripheralUnits getPeripheralUnitsAddresses();
    Checksums getChecksums();
    Credential[] getUsers();
    Credential[] getKeys();
    Expansion[] getExpansions();
    boolean[] getUserEnablings();
    void enableDisableUser(byte userOrdinal, boolean enable);
    void logout();
    // v0.3 APIs
    Keyboard[] getKeyboards();
    ParametersEnablings getParametersEnablings();
    PhoneNumbersSendingCodes getPhoneNumbersSendingCodes();
    PhoneParameters getPhoneParameters();
    PSTNGSM getPSTNGSM();
    Reader[] getReaders();
    SMSs getSMSs();
    TimeProgrammer getTimeProgrammer();
    void armDisarmSectors(byte partitions, byte armingMask);
}
