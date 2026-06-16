package org.paternostro.elkromm;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.PeripheralUnits;

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
    
    public static final int MAX_INPUTS = 8;
    public static final int MAX_OUTPUTS = 6;

    public static final int MAX_DATA_LENGTH = 140;
    public static final int NAME_LENGTH = 24;

    public static final byte BYTE_SOH = 0x01;
    public static final byte BYTE_ETX = 0x03;
    public static final byte BYTE_ACK = 0x06;
    public static final byte BYTE_DC1 = 0x11;
    public static final byte BYTE_NAK = 0x15;
    public static final byte BYTE_SYN = 0x16;

    public static final byte CMD_HELLO = 0x60;
    public static final byte CMD_LOGIN = 0x49;
    public static final byte CMD_SEND = 0x65;
    public static final byte CMD_LOGOUT = 0x63;
    public static final byte CMD_PARTITIONS_AND_AREAS = 0x55;
    public static final byte CMD_SYSTEM_STATUS = (byte)0x80;
    public static final byte CMD_ARM_DISARM_SECTOR = (byte)0x81;
    public static final byte CMD_INPUT_STATUS = (byte)0x84;
    public static final byte CMD_EXPANSIONS = 0x51;
    public static final byte CMD_EXCLUDE_INCLUDE_INPUT = (byte)0x83;
    public static final byte CMD_PERIPHERAL_UNITS_ADDRESSES = 0x62;
    public static final byte CMD_CHECKSUM = 0x50;
    public static final byte CMD_USERS = 0x5b;
    public static final byte CMD_KEYS = 0x5c;
    public static final byte CMD_USER_ENABLINGS = (byte)0x87;
    public static final byte CMD_ENABLE_DISABLE_USER = (byte)0x88;

    enum Partition {
        P_ONE(0x01),
        P_TWO(0x02),
        P_THREE(0x04),
        P_FOUR(0x08),
        P_FIVE(0x10),
        P_SIX(0x20),
        P_SEVEN(0x40),
        P_EIGHT(0x80);

        private byte bitMask;

        Partition(int bitMask)
        {
            this.bitMask = (byte)bitMask;
        }

        public byte getBitMask()
        {
            return bitMask;
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
    boolean[] getSystemStatus();
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
}
