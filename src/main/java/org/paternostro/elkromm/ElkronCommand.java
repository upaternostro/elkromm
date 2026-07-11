package org.paternostro.elkromm;

import org.paternostro.elkromm.packet.AreasAndPartitions;
import org.paternostro.elkromm.packet.ArmDisarmSector;
import org.paternostro.elkromm.packet.C200bParameters;
import org.paternostro.elkromm.packet.Checksums;
import org.paternostro.elkromm.packet.DayClassCommands;
import org.paternostro.elkromm.packet.ElkrommPacket;
import org.paternostro.elkromm.packet.EnableDisableUser;
import org.paternostro.elkromm.packet.ExcludeIncludeInput;
import org.paternostro.elkromm.packet.Expansions;
import org.paternostro.elkromm.packet.Hello;
import org.paternostro.elkromm.packet.InputStatus;
import org.paternostro.elkromm.packet.KeyProgramming;
import org.paternostro.elkromm.packet.Keypads;
import org.paternostro.elkromm.packet.Keys;
import org.paternostro.elkromm.packet.Login;
import org.paternostro.elkromm.packet.Logout;
import org.paternostro.elkromm.packet.PSTNGSM;
import org.paternostro.elkromm.packet.ParametersEnablings;
import org.paternostro.elkromm.packet.PeripheralUnitsAddresses;
import org.paternostro.elkromm.packet.PhoneNumbers;
import org.paternostro.elkromm.packet.PhoneParameters;
import org.paternostro.elkromm.packet.Reader;
import org.paternostro.elkromm.packet.Readers;
import org.paternostro.elkromm.packet.SMSProgramming;
import org.paternostro.elkromm.packet.Send;
import org.paternostro.elkromm.packet.SetAreasAndPartitions;
import org.paternostro.elkromm.packet.SetC200bParameters;
import org.paternostro.elkromm.packet.SetKeys;
import org.paternostro.elkromm.packet.SetPSTNGSM;
import org.paternostro.elkromm.packet.SetParametersEnablings;
import org.paternostro.elkromm.packet.SetPhoneNumbers;
import org.paternostro.elkromm.packet.SetPhoneParameters;
import org.paternostro.elkromm.packet.SetReaders;
import org.paternostro.elkromm.packet.SetSMS;
import org.paternostro.elkromm.packet.SetTimeProgrammer;
import org.paternostro.elkromm.packet.SetUsers;
import org.paternostro.elkromm.packet.SystemStatus;
import org.paternostro.elkromm.packet.TimeProgrammer;
import org.paternostro.elkromm.packet.UserProgramming;
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
    USER_PROGRAMMING(0x95, UserProgramming.class), // No block checksum!
    SET_PARAMETERS_ENABLINGS(0x96, SetParametersEnablings.class),

    SET_TIME_PROGRAMMER(0xe4, SetTimeProgrammer.class),
    SET_PARTITIONS_AND_AREAS(0xe5, SetAreasAndPartitions.class),
    SET_PHONE_PARAMETERS(0xe6, SetPhoneParameters.class),
    SET_PHONE_NUMBERS(0xe7, SetPhoneNumbers.class),
    SET_C200B(0xe8, SetC200bParameters.class),
    SET_SMS(0xe9, SetSMS.class),
    SET_PSTN_GSM(0xea, SetPSTNGSM.class),
    SET_USERS(0xeb, SetUsers.class),
    SET_KEYS(0xec, SetKeys.class),

    SMS_PROGRAMMING(0xa0, SMSProgramming.class), // No block checksum!
    KEYPAD_PROGRAMMING(0x92, null), // FIXME: MISSING // No block checksum!
    EXPANSIONS_PROGRAMMING(0xe1, null), // FIXME: MISSING
    KEYBOARD_PROGRAMMING(0xe2, null), // FIXME: MISSING
    
    KEYPADS(0x52, Keypads.class),
    READERS(0x53, Readers.class),
    READER_PROGRAMMING(0x93, Reader.class), // No block checksum!
    KEY_PROGRAMMING(0xa3, KeyProgramming.class), // No block checksum!
    SET_READERS(0xe3, SetReaders.class),
    
    DAY_CLASS_CMDS(0xa1, DayClassCommands.class); // No block checksum!

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
