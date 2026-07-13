package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.paternostro.elkromm.ElkrommFacade;

public class PhoneNumber implements Serializable {
    public enum Type {
        PNT_PSTN(0x00),
        PNT_GSM(0x01),
        PNT_LAN(0x02);

        private byte value;

        Type(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

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

    public enum SendingMode {
        PNSM_VOICE(0x00),
        PNSM_IDP(0x01),
        PNSM_ADF(0x02),
        PNSM_MODEM(0x04),
        PNSM_SMS(0x06),
        PNSM_C200B(0x07);

        private byte value;

        SendingMode(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

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

    public enum Event {
        PNSCE_BURGLAR_ALARM(0x00e4), // prima occorrenza, poi e8, ec, f0
        PNSCE_PRE_ALARM(0x00f4),
        PNSCE_TECHNOLOGICAL_ALARM_TYPE_1(0x0164),
        PNSCE_TECHNOLOGICAL_ALARM_TYPE_2(0x0168),
        PNSCE_TECHNOLOGICAL_ALARM_TYPE_3(0x016c),
        PNSCE_FIRE_ALARM(0x0110),
        PNSCE_PANIC(0x0108),
        PNSCE_SILENT_PANIC(0x010c),
        PNSCE_MEDICAL_EMERGENCY(0x0120),
        PNSCE_HOLD_UP(0x013c),
        PNSCE_PARTITIONS_SYSTEM_ON_OFF(0x0140),
        PNSCE_MAINTENANCE(0x0150),
        PNSCE_INPUT_INCLUSION_EXCLUSION(0x0144), // prima occorrenza, poi 148
        PNSCE_TAMPERING(0x00cc), // prima occorrenza, poi 104, 170
        PNSCE_MAINS_POWER(0x00d8),
        PNSCE_LOW_BATTERY(0x00d4),
        PNSCE_SYSTEM_FAULT(0x0124), // prima occorrenza, poi 130
        PNSCE_FALSE_CODE(0x0154),
        PNSCE_NOTICES(0x0158);

        private int offset;

        Event(int offset)
        {
            this.offset = offset;
        }

        public int getOffset()
        {
            return offset;
        }

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

    public PhoneNumber(String phoneNumber, boolean[] associatedPartitions, Type type, SendingMode sendingMode, Event[] assignedEvents) {
        setPhoneNumber(phoneNumber);
        setAssociatedPartitions(associatedPartitions);
        setType(type);
        setSendingMode(sendingMode);
        setAssignedEvents(assignedEvents);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        // FIXME: IP addresses in phone numbers!
        if (phoneNumber != null && !phoneNumber.matches("^([0-9]{0,28}|[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:[0-9]{1,5})$")) throw new IllegalArgumentException("Wrong phone number " + phoneNumber);
        
        this.phoneNumber = phoneNumber;
    }

    public boolean[] getAssociatedPartitions() {
        return Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public void setAssociatedPartitions(boolean[] associatedPartitions) {
        if (associatedPartitions == null) throw new IllegalArgumentException("Missing mandatory associated partitions");

        this.associatedPartitions = Arrays.copyOf(associatedPartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public SendingMode getSendingMode() {
        return sendingMode;
    }

    public void setSendingMode(SendingMode sendingMode) {
        this.sendingMode = sendingMode;
    }

    public Iterator<Event> getAssignedEventsIterator() {
        return assignedEvents.iterator();
    }

    public boolean isAssignedEvent(Event event) {
        return assignedEvents.contains(event);
    }

    public void setAssignedEvents(Event[] assignedEvents) {
        this.assignedEvents = new HashSet<Event>();

        addAssignedEvents(assignedEvents);
    }

    public void addAssignedEvents(Event[] assignedEvents) {
        for (Event pivot : assignedEvents) {
            addAssignedEvent(pivot);
        }
    }

    public void addAssignedEvent(Event assignedEvent) {
        this.assignedEvents.add(assignedEvent);
    }
}
