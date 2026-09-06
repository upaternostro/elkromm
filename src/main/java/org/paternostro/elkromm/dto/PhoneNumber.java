package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A single configured phone number: destination, associated partitions, how
 * it's dialed ({@link Type}/{@link SendingMode}), and which events it's set
 * up to report ({@link Event}).
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PhoneNumber implements Serializable {
    /** Which network a phone number is dialed over. */
    public enum Type {
        /** Ordinary landline (PSTN). */
        PNT_PSTN(0x00),
        /** GSM/mobile network. */
        PNT_GSM(0x01),
        /** LAN/IP destination (an IP:port pair rather than a real phone number). */
        PNT_LAN(0x02);

        private byte value;

        Type(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this type.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Type} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching type, or {@code null} if none matches
         */
        public static Type valueOf(byte value) {
            Type  retval = null;

            for (Type pivot : Type.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /** How an event is communicated to a phone number. */
    public enum SendingMode {
        /** Voice message. */
        PNSM_VOICE(0x00),
        /** IDP (digital protocol) reporting. */
        PNSM_IDP(0x01),
        /** ADF (differential ademco format) reporting. */
        PNSM_ADF(0x02),
        /** Modem/data reporting. */
        PNSM_MODEM(0x04),
        /** SMS reporting. */
        PNSM_SMS(0x06),
        /** C200B remote alarm receiver protocol. */
        PNSM_C200B(0x07);

        private byte value;

        SendingMode(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this sending mode.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code SendingMode} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching sending mode, or {@code null} if none matches
         */
        public static SendingMode valueOf(byte value) {
            SendingMode  retval = null;

            for (SendingMode pivot : SendingMode.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /**
     * An event a phone number can be assigned to report.
     * <p>
     * Each constant carries the byte offset of its assignment bit within the
     * {@code SET_PHONE_NUMBERS}/{@code PHONE_NUMBERS} on-the-wire payload;
     * see {@code PROTOCOL-ITA.md} for the full capture this was derived
     * from. Some offsets were seen to shift across repeated events of the
     * same kind during reverse engineering (noted where observed); only the
     * first observed offset is kept here.
     */
    public enum Event {
        /** Burglary alarm. */
        PNSCE_BURGLAR_ALARM(0x00e4), // prima occorrenza, poi e8, ec, f0
        /** Pre-alarm. */
        PNSCE_PRE_ALARM(0x00f4),
        /** Technical alarm, first type. */
        PNSCE_TECHNOLOGICAL_ALARM_TYPE_1(0x0164),
        /** Technical alarm, second type. */
        PNSCE_TECHNOLOGICAL_ALARM_TYPE_2(0x0168),
        /** Technical alarm, third type. */
        PNSCE_TECHNOLOGICAL_ALARM_TYPE_3(0x016c),
        /** Fire alarm. */
        PNSCE_FIRE_ALARM(0x0110),
        /** Panic. */
        PNSCE_PANIC(0x0108),
        /** Silent panic. */
        PNSCE_SILENT_PANIC(0x010c),
        /** Medical emergency. */
        PNSCE_MEDICAL_EMERGENCY(0x0120),
        /** Hold-up. */
        PNSCE_HOLD_UP(0x013c),
        /** Partition/system armed or disarmed. */
        PNSCE_PARTITIONS_SYSTEM_ON_OFF(0x0140),
        /** Maintenance required. */
        PNSCE_MAINTENANCE(0x0150),
        /** Input excluded or re-included. */
        PNSCE_INPUT_INCLUSION_EXCLUSION(0x0144), // prima occorrenza, poi 148
        /** Tampering. */
        PNSCE_TAMPERING(0x00cc), // prima occorrenza, poi 104, 170
        /** Mains power event. */
        PNSCE_MAINS_POWER(0x00d8),
        /** Low battery. */
        PNSCE_LOW_BATTERY(0x00d4),
        /** System fault. */
        PNSCE_SYSTEM_FAULT(0x0124), // prima occorrenza, poi 130
        /** False code entered. */
        PNSCE_FALSE_CODE(0x0154),
        /** Generic notice. */
        PNSCE_NOTICES(0x0158);

        private int offset;

        Event(int offset)
        {
            this.offset = offset;
        }

        /**
         * Returns the byte offset of this event's assignment bit within the
         * phone numbers payload.
         *
         * @return the offset
         */
        public int getOffset()
        {
            return offset;
        }

        /**
         * Looks up the {@code Event} matching a raw byte offset.
         *
         * @param offset the offset to look up
         * @return the matching event, or {@code null} if none matches
         */
        public static Event valueOf(int offset) {
            Event  retval = null;

            for (Event pivot : Event.values()) {
                if (pivot.getOffset() == offset) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private String      phoneNumber;
    private boolean[]   associatedPartitions;
    private Type        type;
    private SendingMode sendingMode;
    private Set<Event>  assignedEvents;

    /**
     * Creates a new phone number.
     *
     * @param phoneNumber the destination, either a digit string (up to {@link ElkrommFacade#PHONE_NUMBER_LENGTH} digits) or an {@code ip:port} pair for {@link Type#PNT_LAN}
     * @param associatedPartitions per-partition association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     * @param type which network this number is dialed over
     * @param sendingMode how events are communicated to this number
     * @param assignedEvents which events this number reports
     */
    public PhoneNumber(String phoneNumber, boolean[] associatedPartitions, Type type, SendingMode sendingMode, Event[] assignedEvents) {
        setPhoneNumber(phoneNumber);
        setAssociatedPartitions(associatedPartitions);
        setType(type);
        setSendingMode(sendingMode);
        setAssignedEvents(assignedEvents);
    }

    /**
     * Returns the destination of this phone number.
     *
     * @return the phone number (or {@code ip:port} pair)
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the destination of this phone number.
     *
     * @param phoneNumber a digit string (up to {@link ElkrommFacade#PHONE_NUMBER_LENGTH} digits) or an {@code ip:port} pair
     * @throws IllegalArgumentException if neither form matches
     */
    public void setPhoneNumber(String phoneNumber) {
        // FIXME: IP addresses in phone numbers!
        if (phoneNumber != null && !phoneNumber.matches("^([0-9]{0,28}|[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5})$")) throw new IllegalArgumentException("Wrong phone number " + phoneNumber);
        
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns a copy of this phone number's per-partition association flags.
     *
     * @return the association flags, length {@link ElkrommFacade#MAX_PARTITIONS}
     */
    public boolean[] getAssociatedPartitions() {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Sets this phone number's per-partition association flags.
     *
     * @param associatedPartitions the flags to set, not {@code null}
     * @throws IllegalArgumentException if {@code associatedPartitions} is {@code null}
     */
    public void setAssociatedPartitions(boolean[] associatedPartitions) {
        if (associatedPartitions == null) throw new IllegalArgumentException("Missing mandatory associated partitions");

        this.associatedPartitions = Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    /**
     * Returns which network this number is dialed over.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets which network this number is dialed over.
     *
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns how events are communicated to this number.
     *
     * @return the sending mode
     */
    public SendingMode getSendingMode() {
        return sendingMode;
    }

    /**
     * Sets how events are communicated to this number.
     *
     * @param sendingMode the sending mode to set
     */
    public void setSendingMode(SendingMode sendingMode) {
        this.sendingMode = sendingMode;
    }

    /**
     * Returns an iterator over the events this number reports.
     *
     * @return the iterator
     */
    public Iterator<Event> getAssignedEventsIterator() {
        return assignedEvents.iterator();
    }

    /**
     * Checks whether this number reports a given event.
     *
     * @param event the event to check
     * @return {@code true} if assigned
     */
    public boolean isAssignedEvent(Event event) {
        return assignedEvents.contains(event);
    }

    /**
     * Replaces the full set of events this number reports.
     *
     * @param assignedEvents the events to assign
     */
    public void setAssignedEvents(Event[] assignedEvents) {
        this.assignedEvents = new HashSet<Event>();

        addAssignedEvents(assignedEvents);
    }

    /**
     * Adds events to the set this number reports, without clearing existing ones.
     *
     * @param assignedEvents the events to add
     */
    public void addAssignedEvents(Event[] assignedEvents) {
        for (Event pivot : assignedEvents) {
            addAssignedEvent(pivot);
        }
    }

    /**
     * Adds a single event to the set this number reports.
     *
     * @param assignedEvent the event to add
     */
    public void addAssignedEvent(Event assignedEvent) {
        this.assignedEvents.add(assignedEvent);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{phoneNumber=").append(phoneNumber).append(", associatedPartitions="
               ).append(Arrays.toString(associatedPartitions)).append(", type=").append(type).append(", sendingMode=").append(sendingMode
               ).append(", assignedEvents=").append(assignedEvents).append("}");
        
        return sb.toString();
    }
}
