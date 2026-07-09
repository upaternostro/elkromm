package org.paternostro.elkromm;

import org.paternostro.elkromm.packet.AreasAndPartitions;
import org.paternostro.elkromm.packet.ArmDisarmSector;
import org.paternostro.elkromm.packet.C200bParameters;
import org.paternostro.elkromm.packet.Checksums;
import org.paternostro.elkromm.packet.ElkrommPacket;
import org.paternostro.elkromm.packet.EnableDisableUser;
import org.paternostro.elkromm.packet.ExcludeIncludeInput;
import org.paternostro.elkromm.packet.Expansions;
import org.paternostro.elkromm.packet.Hello;
import org.paternostro.elkromm.packet.InputStatus;
import org.paternostro.elkromm.packet.Keys;
import org.paternostro.elkromm.packet.Login;
import org.paternostro.elkromm.packet.Logout;
import org.paternostro.elkromm.packet.PSTNGSM;
import org.paternostro.elkromm.packet.ParametersEnablings;
import org.paternostro.elkromm.packet.PeripheralUnitsAddresses;
import org.paternostro.elkromm.packet.PhoneNumbers;
import org.paternostro.elkromm.packet.PhoneParameters;
import org.paternostro.elkromm.packet.Send;
import org.paternostro.elkromm.packet.SetParametersEnablings;
import org.paternostro.elkromm.packet.SystemStatus;
import org.paternostro.elkromm.packet.TimeProgrammer;
import org.paternostro.elkromm.packet.UserEnablings;
import org.paternostro.elkromm.packet.Users;

public enum ElkronCommand {
    HELLO(0x60, Hello.class),
    LOGIN(0x49, Login.class),
    SEND(0x65, Send.class),
    LOGOUT(0x63, Logout.class),
    PARTITIONS_AND_AREAS(0x55, AreasAndPartitions.class),
    SYSTEM_STATUS(0x80, SystemStatus.class),
    ARM_DISARM_SECTOR(0x81, ArmDisarmSector.class),
    INPUT_STATUS(0x84, InputStatus.class),
    EXPANSIONS(0x51, Expansions.class),
    EXCLUDE_INCLUDE_INPUT(0x83, ExcludeIncludeInput.class),
    PERIPHERAL_UNITS_ADDRESSES(0x62, PeripheralUnitsAddresses.class), // No block checksum!
    CHECKSUM(0x50, Checksums.class), // No block checksum!
    USERS(0x5b, Users.class),
    KEYS(0x5c, Keys.class),
    PARAMETERS_ENABLINGS(0x26, ParametersEnablings.class),
    USER_ENABLINGS(0x87, UserEnablings.class),
    PHONE_NUMBERS(0x57, PhoneNumbers.class),
    PHONE_PARAMETERS(0x56, PhoneParameters.class),
    PSTN_GSM(0x5a, PSTNGSM.class),
    SMS(0x59, org.paternostro.elkromm.packet.SMS.class),
    C200B(0x58, C200bParameters.class),
    TIME_PROGRAMMER(0x54, TimeProgrammer.class),
    ENABLE_DISABLE_USER(0x88, EnableDisableUser.class),

    KEY_STATUS(0x8b, null), // FIXME: MISSING

    EVENT_LOG(0x70, null), // FIXME: MISSING    
    
    CONTROL_PANEL_PROGRAMMING(0x91, null), // FIXME: MISSING
    USER_PROGRAMMING(0x95, null), // FIXME: MISSING
    SET_PARAMETERS_ENABLINGS(0x96, SetParametersEnablings.class), // FIXME: sicuri che ne serva uno doverso per SET?

    SET_TIME_PROGRAMMER(0xe4, null), // FIXME: MISSING
    SET_PARTITIONS_AND_AREAS(0xe5, null), // FIXME: MISSING
    SET_PHONE_PARAMETERS(0xe6, null), // FIXME: MISSING
    SET_PHONE_NUMBERS(0xe7, null), // FIXME: MISSING
    SET_C200B(0xe8, null), // FIXME: MISSING
    SET_SMS(0xe9, null), // FIXME: MISSING
    SET_PSTN_GSM(0xea, null), // FIXME: MISSING
    SET_USERS(0xeb, null), // FIXME: MISSING
    SET_KEYS(0xec, null), // FIXME: MISSING

    SMS_PROGRAMMING(0xa0, null), // FIXME: MISSING // No block checksum!
    KEYPAD_PROGRAMMING(0x92, null), // FIXME: MISSING // No block checksum!
    EXPANSIONS_PROGRAMMING(0xe1, null), // FIXME: MISSING
    KEYBOARD_PROGRAMMING(0xe2, null), // FIXME: MISSING
    
    KEYPADS(0x52, null), // FIXME: MISSING
    READERS(0x53, null), // FIXME: MISSING
    READER_PROGRAMMING(0x93, null), // FIXME: MISSING
    SET_READERS(0xe3, null), // FIXME: MISSING
    
    DAY_CLASS_CMDS(0xa1, null); // FIXME: MISSING // No Block checksum!

    protected int                               value;
    protected Class<? extends ElkrommPacket>    packetClass;

    ElkronCommand(int value, Class<? extends ElkrommPacket> packetClass) {
        this.value = value & 0xFF;
        this.packetClass = packetClass;
    }

    public int getValue() {
        return value;
    }

    public Class<? extends ElkrommPacket> getPacketClass() {
        return packetClass;
    }

    public static ElkronCommand valueOf(int value) {
        for (ElkronCommand pivot : values()) {
            if (pivot.getValue() == value) {
                return pivot;
            }
        }

        return null;
    }
}
