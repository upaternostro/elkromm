package org.paternostro.elkromm;

import java.io.IOException;
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
import org.paternostro.elkromm.dto.SingleCredential;
import org.paternostro.elkromm.dto.SingleKeyboard;
import org.paternostro.elkromm.dto.SingleSMS;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
import org.paternostro.mock.ipc.Endpoint;

/**
 * Main entry point of the elkromm library.
 * <p>
 * Provides access to the Elkron MP-508 alarm panel protocol as implemented by
 * the Hi-Connect configuration software: connection lifecycle, reading the
 * panel's configuration and live status, and writing configuration changes.
 * An instance is obtained through {@link ElkrommFactory}, never implemented
 * directly by client code.
 * <p>
 * Typical lifecycle: {@link #init(InetAddress, int, int)} (or the
 * {@link #init(Endpoint, int)} variant used for testing against
 * {@code elkromm}'s own emulator), {@link #connect()},
 * {@link #login(int, int)}, then any number of read/write operations,
 * finished by {@link #logout()} and {@link #disconnect()}.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public interface ElkrommFacade
{
    /** Connection lifecycle state, as tracked by {@link #getStatus()}. */
    enum Status {
        /** No {@code init} call has been made yet. */
        ST_NOT_INITIALIZED,
        /** Initialized but not (or no longer) connected to a panel. */
        ST_DISCONNECTED,
        /** Connected, but {@link #login(int, int)} has not completed yet. */
        ST_CONNECTED,
        /** Connected and logged in; read/write operations are allowed. */
        ST_LOGGED_IN
    }

    /** Number of areas supported by the panel. */
    public static final int MAX_AREAS = 4;
    /** Number of partitions (sectors) supported by the panel. */
    public static final int MAX_PARTITIONS = 8;
    /** Number of user/key credentials supported by the panel. */
    public static final int MAX_CREDENTIALS = 32;

    /** Number of proximity key readers supported by the panel. */
    public static final int MAX_READERS = 16;
    /** Number of expansion units supported by the panel. */
    public static final int MAX_EXPANSIONS = 7;
    /** Number of keypads supported by the panel. */
    public static final int MAX_KEYPADS = 8;

    /** Number of logical inputs (sensors) supported by the panel. */
    public static final int MAX_LOGICAL_INPUTS = 64;
    /** Number of physical inputs on a single expansion unit. */
    public static final int MAX_EXP_INPUTS = 8;
    /** Number of physical outputs on a single expansion unit. */
    public static final int MAX_EXP_OUTPUTS = 6;

    /** Number of phone numbers storable by the panel. */
    public static final int MAX_PHONE_NUMBERS = 12;
    /** Maximum length, in digits, of a single phone number. */
    public static final int PHONE_NUMBER_LENGTH = 28; // digits!

    /** Number of configurable SMS message slots. */
    public static final int MAX_SMS = 9;

    /** Maximum payload length of a single on-the-wire packet. */
    public static final int MAX_DATA_LENGTH = 140;
    /** Maximum length of a user/key/etc. display name. */
    public static final int NAME_LENGTH = 24;
    /** Maximum length of a single SMS message. */
    public static final int SMS_LENGTH = 40;
    /** Number of scheduled commands per day class ({@link org.paternostro.elkromm.dto.DayClassCommands}). */
    public static final int NUM_COMMANDS = 8;

    /** Framing control byte: Start Of Header. */
    public static final byte BYTE_SOH = 0x01;
    /** Framing control byte: End of TeXt. */
    public static final byte BYTE_ETX = 0x03;
    /** Framing control byte: ACKnowledge. */
    public static final byte BYTE_ACK = 0x06;
    /** Framing control byte: Device Control 1, used to escape other control bytes appearing in payload data. */
    public static final byte BYTE_DC1 = 0x11;
    /** Framing control byte: Negative AcKnowledge. */
    public static final byte BYTE_NAK = 0x15;
    /** Framing control byte: SYNchronous idle, used by the panel as a generic "OK" reply. */
    public static final byte BYTE_SYN = 0x16;

    /**
     * Bitmask identifying one or more partitions (sectors).
     * <p>
     * {@code P_NONE} and {@code P_ALL} are convenience aliases, not real
     * single-bit values: {@code P_NONE} means no bit set, {@code P_ALL}
     * means every bit set.
     */
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

        /**
         * Returns the raw bitmask value of this partition.
         *
         * @return the single-bit (or, for {@code P_NONE}/{@code P_ALL}, sentinel) mask
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Partition} matching an exact raw bitmask value.
         *
         * @param value the raw byte value to look up
         * @return the matching {@code Partition}, or {@code null} if none matches
         */
        public static Partition valueOf(byte value)
        {
            for (Partition pivot : Partition.values()) {
                if (pivot.getValue() == value) {
                    return pivot;
                }
            }

            return null;
        }

        /**
         * Checks whether a given partition's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param partition the single partition bit to look for
         * @return {@code true} if {@code partition}'s bit is set in {@code value}
         */
        public static boolean is(byte value, Partition partition) {
            return (value & partition.getValue()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid partition bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #P_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((P_ALL.getValue() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /**
     * Bitmask of status flags for a single input (sensor), as returned by
     * {@link #getInputStatus()}.
     * <p>
     * {@code IS_CLOSED} is <strong>not</strong> a real bit: an input is
     * considered closed when {@code IS_OPEN} is absent, not by testing a
     * dedicated bit. {@code IS_ALL} is a convenience alias matching every
     * real bit.
     */
    // bitmask!
    enum InputStatus {
        IS_CLOSED(0x00), // WARNING: not a real bit mask! To select a closed input, search for a not open one ;)
        IS_TAMPER(0x01),
        IS_OPEN(0x02),
        IS_ALARM_MEMORY(0x04),
        IS_TAMPER_MEMORY(0x08),
        IS_EXCLUDED(0x10),
        IS_TEMPORARY_EXCLUDED(0x20),
        IS_ALL(0x3f);   // hack to be always true ;)

        private byte bitMask;

        InputStatus(int bitMask)
        {
            this.bitMask = (byte)bitMask;
        }

        /**
         * Returns the raw bitmask value of this status flag.
         *
         * @return the single-bit (or sentinel) mask
         */
        public byte getBitMask()
        {
            return bitMask;
        }

        /**
         * Looks up the {@code InputStatus} matching an exact raw bitmask value.
         *
         * @param value the raw byte value to look up
         * @return the matching {@code InputStatus}, or {@code null} if none matches
         */
        public static InputStatus valueOf(byte value)
        {
            for (InputStatus pivot : InputStatus.values()) {
                if (pivot.getBitMask() == value) {
                    return pivot;
                }
            }

            return null;
        }

        /**
         * Checks whether a given status flag's bit is set in a bitmask.
         *
         * @param value the bitmask to test
         * @param status the single status bit to look for
         * @return {@code true} if {@code status}'s bit is set in {@code value}
         */
        public static boolean is(byte value, InputStatus status) {
            return (value & status.getBitMask()) != 0;
        }

        /**
         * Checks whether a bitmask contains only valid status bits.
         *
         * @param bitmask the bitmask to validate
         * @return {@code true} if no bit outside {@link #IS_ALL} is set
         */
        public static boolean isValid(byte bitmask) {
            return (bitmask & ((IS_ALL.getBitMask() ^ 0xFF) & 0xFF)) == 0;
        }
    }

    /**
     * Sets the delay used to communicate to the panel, whose hardware is not so much performant.
     * Hint: set to 0 (zero) to obtain maximum speed (i.e.: during unit test that do not use a hardware panel)
     * 
     * @param delay delay in ms, defaults to {@link org.paternostro.elkromm.impl.ElkrommFacadeImpl#DEFAULT_DELAY}
     * @throws ElkrommException if the parameter is less than zero
     */
    void setDelay(int delay) throws ElkrommException;
    
    /**
     * Initializes this facade to connect to a real panel over TCP/IP.
     *
     * @param inetAddr address of the panel's LAN expansion board
     * @param port TCP port the panel is listening on (protocol default: 8030)
     * @param plantCode installer/plant identification code, used later at {@link #login(int, int)} time
     * @throws IOException if the underlying endpoint cannot be prepared
     */
    void init(InetAddress inetAddr, int port, int plantCode) throws IOException;

    /**
     * Initializes this facade around an already-built {@link Endpoint}.
     * <p>
     * Used to connect to the bundled emulator (see {@code org.paternostro.elkromm.emulator})
     * instead of a real panel, typically via an in-memory pipe endpoint from {@code mock-ipc}.
     *
     * @param endpoint the endpoint to communicate through
     * @param plantCode installer/plant identification code, used later at {@link #login(int, int)} time
     */
    void init(Endpoint endpoint, int plantCode);

    /**
     * Returns the current connection lifecycle state.
     *
     * @return the current {@link Status}
     */
    Status getStatus();

    /**
     * Opens the connection to the panel (or emulator) configured by {@code init}.
     *
     * @throws ElkrommException if the connection cannot be established
     */
    void connect() throws ElkrommException;

    /**
     * Closes the connection to the panel (or emulator).
     *
     * @throws ElkrommException if an error occurs while disconnecting
     */
    void disconnect() throws ElkrommException;

    /**
     * Performs the HELLO/LOGIN/SEND handshake required before any other
     * operation is allowed.
     *
     * @param plantCode installer/plant identification code
     * @param technicalCode installer's technical access code
     * @throws ElkrommException if the handshake fails (e.g. wrong codes, protocol error)
     */
    void login(int plantCode, int technicalCode) throws ElkrommException;

    /**
     * Sends a keepalive (SEND) to the panel.
     *
     * @throws ElkrommException if the keepalive fails
     */
    void ping() throws ElkrommException;

    /**
     * Reads areas and partitions configuration ("blocco A").
     *
     * @return the areas/partitions configuration
     * @throws ElkrommException if the read fails
     */
    AreasAndPartitions getAreasAndPartitions() throws ElkrommException;

    /**
     * Reads which partitions are currently armed.
     *
     * @return the current system (partition arming) status
     * @throws ElkrommException if the read fails
     */
    SystemStatus getSystemStatus() throws ElkrommException;

    /**
     * Arms or disarms a single partition.
     *
     * @param sector the partition to act on
     * @param arm {@code true} to arm, {@code false} to disarm
     * @throws ElkrommException if the command fails
     */
    void armDisarmSector(Partition sector, boolean arm) throws ElkrommException;

    /**
     * Reads the raw per-input status byte array, one byte per input, exactly
     * as returned by the panel.
     *
     * @return the raw input status bytes
     * @throws ElkrommException if the read fails
     */
    byte[] getRawInputStatus() throws ElkrommException;

    /**
     * Reads the status of all inputs, grouped by {@link InputStatus} flag.
     *
     * @return a map from each status flag to the (1-based) ordinals of the inputs currently in that state
     * @throws ElkrommException if the read fails
     */
    Map<InputStatus,List<Integer>> getInputStatus() throws ElkrommException;

    /**
     * Excludes or re-includes a single input from the alarm logic.
     *
     * @param inputOrdinal 1-based ordinal of the input to act on
     * @param exclude {@code true} to exclude, {@code false} to include
     * @throws ElkrommException if the command fails
     */
    void excludeIncludeInput(byte inputOrdinal, boolean exclude) throws ElkrommException;

    /**
     * Reads the addresses of all peripheral units (keypads, readers, expansions).
     *
     * @return the peripheral unit addresses
     * @throws ElkrommException if the read fails
     */
    PeripheralUnits getPeripheralUnitsAddresses() throws ElkrommException;

    /**
     * Reads the per-block checksums maintained by the panel, useful to detect
     * configuration changes without re-reading every block.
     *
     * @return the current checksums
     * @throws ElkrommException if the read fails
     */
    Checksums getChecksums() throws ElkrommException;

    /**
     * Reads all user credentials.
     *
     * @return the configured users
     * @throws ElkrommException if the read fails
     */
    Credential[] getUsers() throws ElkrommException;

    /**
     * Reads all proximity key credentials.
     *
     * @return the configured keys
     * @throws ElkrommException if the read fails
     */
    Credential[] getKeys() throws ElkrommException;

    /**
     * Reads all expansion units ("blocco B").
     *
     * @return the configured expansions
     * @throws ElkrommException if the read fails
     */
    Expansion[] getExpansions() throws ElkrommException;

    /**
     * Reads which users are currently enabled.
     *
     * @return a flag array, one entry per user, {@code true} if enabled
     * @throws ElkrommException if the read fails
     */
    boolean[] getUserEnablings() throws ElkrommException;

    /**
     * Enables or disables a single user.
     *
     * @param userOrdinal 1-based ordinal of the user to act on (user 0 is TECNICO/installer)
     * @param enable {@code true} to enable, {@code false} to disable
     * @throws ElkrommException if the command fails
     */
    void enableDisableUser(byte userOrdinal, boolean enable) throws ElkrommException;

    /**
     * Logs out of the current session, without closing the underlying connection.
     *
     * @throws ElkrommException if the logout fails
     */
    void logout() throws ElkrommException;

    // v0.3 APIs
    /**
     * Reads all configured keypads.
     *
     * @return the configured keypads
     * @throws ElkrommException if the read fails
     */
    Keyboard[] getKeyboards() throws ElkrommException;

    /**
     * Reads general system parameters and enablings.
     *
     * @return the current parameters/enablings
     * @throws ElkrommException if the read fails
     */
    ParametersEnablings getParametersEnablings() throws ElkrommException;

    /**
     * Reads the per-phone-number event sending codes.
     *
     * @return the current sending codes
     * @throws ElkrommException if the read fails
     */
    PhoneNumbersSendingCodes getPhoneNumbersSendingCodes() throws ElkrommException;

    /**
     * Reads the telephone dialer parameters.
     *
     * @return the current phone parameters
     * @throws ElkrommException if the read fails
     */
    PhoneParameters getPhoneParameters() throws ElkrommException;

    /**
     * Reads the PSTN/GSM line configuration.
     *
     * @return the current PSTN/GSM configuration
     * @throws ElkrommException if the read fails
     */
    PSTNGSM getPSTNGSM() throws ElkrommException;

    /**
     * Reads all configured proximity key readers.
     *
     * @return the configured readers
     * @throws ElkrommException if the read fails
     */
    Reader[] getReaders() throws ElkrommException;

    /**
     * Reads the configured SMS message texts.
     *
     * @return the configured SMS messages
     * @throws ElkrommException if the read fails
     */
    SMSs getSMSs() throws ElkrommException;

    /**
     * Reads the C200B (remote alarm receiver) configuration.
     *
     * @return the current C200B configuration
     * @throws ElkrommException if the read fails
     */
    C200bParameters getC200bParameters() throws ElkrommException;

    /**
     * Reads the weekly time programmer configuration.
     *
     * @return the current time programmer configuration
     * @throws ElkrommException if the read fails
     */
    TimeProgrammer getTimeProgrammer() throws ElkrommException;

    /**
     * Arms or disarms multiple partitions in a single command.
     *
     * @param partitions bitmask (see {@link Partition}) selecting which partitions to act on
     * @param armingMask bitmask of the same shape, with a bit set for each partition to arm
     * @throws ElkrommException if the command fails
     */
    void armDisarmSectors(byte partitions, byte armingMask) throws ElkrommException;

    // setters
    /**
     * Writes general system parameters and enablings.
     *
     * @param parametersEnablings the parameters/enablings to write
     * @throws ElkrommException if the write fails
     */
    void setParametersEnablings(ParametersEnablings parametersEnablings) throws ElkrommException;

    /**
     * Writes areas and partitions configuration ("blocco A").
     *
     * @param areasAndPartitions the configuration to write
     * @throws ElkrommException if the write fails
     */
    void setAreasAndPartitions(AreasAndPartitions areasAndPartitions) throws ElkrommException;

    /**
     * Writes the telephone dialer parameters.
     *
     * @param phoneParameters the parameters to write
     * @throws ElkrommException if the write fails
     */
    void setPhoneParameters(PhoneParameters phoneParameters) throws ElkrommException;

    /**
     * Writes the per-phone-number event sending codes.
     *
     * @param phoneNumbersSendingCodes the sending codes to write
     * @throws ElkrommException if the write fails
     */
    void setPhoneNumbersSendingCodes(PhoneNumbersSendingCodes phoneNumbersSendingCodes) throws ElkrommException;

    /**
     * Writes the C200B (remote alarm receiver) configuration.
     *
     * @param c200bParameters the configuration to write
     * @throws ElkrommException if the write fails
     */
    void setC200bParameters(C200bParameters c200bParameters) throws ElkrommException;

    /**
     * Writes all configured SMS message texts in a single block.
     *
     * @param sMSs the SMS messages to write
     * @throws ElkrommException if the write fails
     */
    void setSMSs(SMSs sMSs) throws ElkrommException;

    /**
     * Writes the PSTN/GSM line configuration.
     *
     * @param pSTNGSM the configuration to write
     * @throws ElkrommException if the write fails
     */
    void setPSTNGSM(PSTNGSM pSTNGSM) throws ElkrommException;

    /**
     * Writes all user credentials in a single block.
     *
     * @param users the users to write
     * @throws ElkrommException if the write fails
     */
    void setUsers(Credential[] users) throws ElkrommException;

    /**
     * Writes all proximity key credentials in a single block.
     *
     * @param keys the keys to write
     * @throws ElkrommException if the write fails
     */
    void setKeys(Credential[] keys) throws ElkrommException;

    /**
     * Writes a single SMS message, identified by its index.
     *
     * @param sMS the message (with its index) to write
     * @throws ElkrommException if the write fails
     */
    void setSMS(SingleSMS sMS) throws ElkrommException;

    /**
     * Writes the configuration of a single keypad, identified by its index.
     *
     * @param keyboard the keypad configuration (with its index) to write
     * @throws ElkrommException if the write fails
     */
    void setKeyboard(SingleKeyboard keyboard) throws ElkrommException;

    /**
     * Writes the configuration of a single proximity key reader.
     * <p>
     * Unlike other single-instance writes, the reader is identified by the
     * {@code address} field of {@link Reader} itself rather than by a
     * separate index wrapper.
     *
     * @param reader the reader configuration to write
     * @throws ElkrommException if the write fails
     */
    void setReader(Reader reader) throws ElkrommException;

    /**
     * Writes the scheduled commands for a single day class (working day,
     * pre-holiday or holiday).
     *
     * @param dayClassCommands the day class and its scheduled commands
     * @throws ElkrommException if the write fails
     */
    void setDayClassCommands(DayClassCommands dayClassCommands) throws ElkrommException;

    // v0.5 APIs
    /**
     * Writes the configuration of a single user, identified by its index.
     *
     * @param user the user configuration (with its index) to write
     * @throws ElkrommException if the write fails
     */
    void setUser(SingleCredential user) throws ElkrommException;

    /**
     * Writes the configuration of a single key, identified by its index.
     *
     * @param key the key configuration (with its index) to write
     * @throws ElkrommException if the write fails
     */
    void setKey(SingleCredential key) throws ElkrommException;
}
