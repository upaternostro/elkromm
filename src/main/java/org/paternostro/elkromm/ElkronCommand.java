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
import org.paternostro.elkromm.packet.KeypadProgramming;
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

/**
 * Every command code of the Elkron/Hi-Connect protocol, paired with the
 * {@link ElkrommPacket} subclass (if any) that knows how to frame it.
 * <p>
 * Commands whose {@code packetClass} is {@code null} are known to exist
 * (from captured Hi-Connect traffic) but have no implementation yet; see
 * {@code PROTOCOL-ITA.md} in the project root for the full protocol
 * reference, including read/write/action semantics for each command.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public enum ElkronCommand {
    /** Session control: first packet of the login handshake. */
    HELLO(0x60, Hello.class),
    /** Session control: second packet of the login handshake, carries the plant/installer codes. */
    LOGIN(0x49, Login.class),
    /** Session control: keepalive, also used by Hi-Connect to poll for pending data. */
    SEND(0x65, Send.class),
    /** Session control: ends the current session. */
    LOGOUT(0x63, Logout.class),
    /** Read: areas and partitions configuration ("blocco A"). */
    PARTITIONS_AND_AREAS(0x55, AreasAndPartitions.class),
    /** Read: which partitions are currently armed. */
    SYSTEM_STATUS(0x80, SystemStatus.class),
    /** Action: arms or disarms a single partition. */
    ARM_DISARM_SECTOR(0x81, ArmDisarmSector.class),
    /** Read: per-input status flags. */
    INPUT_STATUS(0x84, InputStatus.class), // No block checksum!
    /** Read: expansion units configuration ("blocco B"). */
    EXPANSIONS(0x51, Expansions.class),
    /** Action: excludes or re-includes a single input. */
    EXCLUDE_INCLUDE_INPUT(0x83, ExcludeIncludeInput.class),
    /** Read: addresses of all peripheral units (keypads, readers, expansions). */
    PERIPHERAL_UNITS_ADDRESSES(0x62, PeripheralUnitsAddresses.class), // No block checksum!
    /** Read: per-block checksums, to detect configuration changes cheaply. */
    CHECKSUM(0x50, Checksums.class), // No block checksum!
    /** Read: user credentials. */
    USERS(0x5b, Users.class),
    /** Read: proximity key credentials. */
    KEYS(0x5c, Keys.class),
    /** Read: general system parameters and enablings. */
    PARAMETERS_ENABLINGS(0x26, ParametersEnablings.class),
    /** Read: which users are currently enabled. */
    USER_ENABLINGS(0x87, UserEnablings.class),
    /** Read: configured phone numbers. */
    PHONE_NUMBERS(0x57, PhoneNumbers.class),
    /** Read: telephone dialer parameters. */
    PHONE_PARAMETERS(0x56, PhoneParameters.class),
    /** Read: PSTN/GSM line configuration. */
    PSTN_GSM(0x5a, PSTNGSM.class),
    /** Read: configured SMS message texts. */
    SMS(0x59, org.paternostro.elkromm.packet.SMS.class),
    /** Read: C200B (remote alarm receiver) configuration. */
    C200B(0x58, C200bParameters.class),
    /** Read: weekly time programmer configuration. */
    TIME_PROGRAMMER(0x54, TimeProgrammer.class),
    /** Action: enables or disables a single user. */
    ENABLE_DISABLE_USER(0x88, EnableDisableUser.class),

    /** Read (hypothesis, by naming analogy with the other {@code *_STATUS} commands): key/reader status. Not yet implemented. */
    KEY_STATUS(0x8b, null), // FIXME: MISSING

    /** Read (hypothesis): event log. Not yet implemented; see project notes on remote log access. */
    EVENT_LOG(0x70, null), // FIXME: MISSING    
    
    /** Single-instance write (hypothesis, by naming analogy with the other {@code *_PROGRAMMING} commands). Not yet implemented. */
    CONTROL_PANEL_PROGRAMMING(0x91, null), // FIXME: MISSING
    /** Single-instance write: adds/updates a single user credential. Not wired to {@link ElkrommFacade}. */
    USER_PROGRAMMING(0x95, UserProgramming.class), // No block checksum!
    /** Write: general system parameters and enablings (shares data layout with {@link #PARAMETERS_ENABLINGS}). */
    SET_PARAMETERS_ENABLINGS(0x96, SetParametersEnablings.class),

    /** Write: weekly time programmer configuration. */
    SET_TIME_PROGRAMMER(0xe4, SetTimeProgrammer.class),
    /** Write: areas and partitions configuration ("blocco A"). */
    SET_PARTITIONS_AND_AREAS(0xe5, SetAreasAndPartitions.class),
    /** Write: telephone dialer parameters. */
    SET_PHONE_PARAMETERS(0xe6, SetPhoneParameters.class),
    /** Write: configured phone numbers. */
    SET_PHONE_NUMBERS(0xe7, SetPhoneNumbers.class),
    /** Write: C200B (remote alarm receiver) configuration. */
    SET_C200B(0xe8, SetC200bParameters.class),
    /** Write: all configured SMS message texts in a single block. */
    SET_SMS(0xe9, SetSMS.class),
    /** Write: PSTN/GSM line configuration. */
    SET_PSTN_GSM(0xea, SetPSTNGSM.class),
    /** Write: all user credentials in a single block. */
    SET_USERS(0xeb, SetUsers.class),
    /** Write: all proximity key credentials in a single block. */
    SET_KEYS(0xec, SetKeys.class),

    /** Single-instance write: a single SMS message, identified by {@code SMSIndex}. */
    SMS_PROGRAMMING(0xa0, SMSProgramming.class), // No block checksum!
    /** Single-instance write: a single keypad's configuration. */
    KEYPAD_PROGRAMMING(0x92, KeypadProgramming.class), // No block checksum!
    /** Single-instance write (hypothesis): a single expansion unit's configuration. Not yet implemented. */
    EXPANSIONS_PROGRAMMING(0xe1, null), // FIXME: MISSING
    /** Single-instance write (hypothesis): purpose unclear; not the same as {@link #KEYPAD_PROGRAMMING}. Not yet implemented. */
    KEYBOARD_PROGRAMMING(0xe2, null), // FIXME: MISSING
    
    /** Read: all configured keypads. */
    KEYPADS(0x52, Keypads.class),
    /** Read: all configured proximity key readers. */
    READERS(0x53, Readers.class),
    /** Single-instance write: a single proximity key reader, identified by its own {@code address} field. Implemented but untested against a real panel (no reader hardware available). */
    READER_PROGRAMMING(0x93, Reader.class), // No block checksum!
    /** Single-instance write: adds/updates a single key credential. Not wired to {@link ElkrommFacade}. */
    KEY_PROGRAMMING(0xa3, KeyProgramming.class), // No block checksum!
    /** Write: all proximity key readers in a single block. */
    SET_READERS(0xe3, SetReaders.class),
    
    /** Single-instance write: the scheduled commands for a single day class (working day/pre-holiday/holiday). */
    DAY_CLASS_CMDS(0xa1, DayClassCommands.class); // No block checksum!

    protected int                               value;
    protected Class<? extends ElkrommPacket>    packetClass;

    ElkronCommand(int value, Class<? extends ElkrommPacket> packetClass) {
        this.value = value & 0xFF;
        this.packetClass = packetClass;
    }

    /**
     * Returns the raw command code byte value (as an {@code int} in range [0, 255]).
     *
     * @return the command code
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the {@link ElkrommPacket} subclass that frames this command, if implemented.
     *
     * @return the packet class, or {@code null} if this command is not yet implemented
     */
    public Class<? extends ElkrommPacket> getPacketClass() {
        return packetClass;
    }

    /**
     * Looks up the {@code ElkronCommand} matching a raw command code.
     *
     * @param value the raw command code to look up
     * @return the matching command, or {@code null} if none matches
     */
    public static ElkronCommand valueOf(int value) {
        for (ElkronCommand pivot : values()) {
            if (pivot.getValue() == value) {
                return pivot;
            }
        }

        return null;
    }
}
