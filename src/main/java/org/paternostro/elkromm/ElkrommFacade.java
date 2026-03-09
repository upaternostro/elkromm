package org.paternostro.elkromm;

import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.User;
import org.paternostro.elkromm.dto.Expansion;

import java.util.List;
import java.util.Map;

public interface ElkrommFacade
{
    enum Status {
        ST_DISCONNECTED,
        ST_CONNECTED,
        ST_LOGGED_IN
    }

    int MAX_PARTITIONS = 8;
    int MAX_USERS = 32;

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
    User[] getUsers();
    Key[] getKeys();
    Expansion[] getExpansions();
    boolean[] getUserEnablings();
    void enableDisableUser(byte userOrdinal, boolean enable);
    void logout();
}
