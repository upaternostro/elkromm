package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * C200B (remote alarm receiver) configuration: the per-event and per-input
 * codes transmitted to a remote monitoring station.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class C200bParameters implements Serializable {
    /**
     * An event the panel can report to the remote receiver.
     * <p>
     * Each constant carries the byte offset of its code within the
     * {@code SET_C200B} on-the-wire payload; see {@code PROTOCOL-ITA.md}
     * for the full capture this was derived from. Offsets marked as
     * "prima occorrenza" (first occurrence) were seen to shift across
     * repeated events of the same kind during reverse engineering; only
     * the first observed offset is kept here.
     */
    public enum Event {
        /** Burglary alarm. */
        C2PE_BURGLAR_ALARM(0x38), // prima occorrenza, poi 39, 3a, 3b
        /** Pre-alarm. */
        C2PE_PRE_ALARM(0x3c),
        /** Technical alarm, first type. */
        C2PE_TECHNOLOGICAL_ALARM_TYPE_1(0x58),
        /** Technical alarm, second type. */
        C2PE_TECHNOLOGICAL_ALARM_TYPE_2(0x59),
        /** Technical alarm, third type. */
        C2PE_TECHNOLOGICAL_ALARM_TYPE_3(0x5a),
        /** Fire alarm. */
        C2PE_FIRE_ALARM(0x43),
        /** Panic. */
        C2PE_PANIC(0x41),
        /** Silent panic. */
        C2PE_SILENT_PANIC(0x42),
        /** Medical emergency. */
        C2PE_MEDICAL_EMERGENCY(0x47),
        /** Hold-up. */
        C2PE_HOLD_UP(0x4e),
        /** System armed or disarmed. */
        C2PE_SYSTEM_ON_OFF(0x4c), // prima occorrenza, poi 4d, 4f
        /** Partition armed or disarmed. */
        C2PE_PARTITIONS_ON_OFF(0x4a),
        /** Maintenance required. */
        C2PE_MAINTENANCE(0x53),
        /** Input excluded or re-included. */
        C2PE_INPUT_INCLUSION_EXCLUSION(0x50), // prima occorrenza, poi 51
        /** Tampering. */
        C2PE_TAMPERING(0x32), // prima occorrenza, poi 40, 5b
        /** Mains power event. */
        C2PE_MAINS_POWER(0x35),
        /** Low battery. */
        C2PE_LOW_BATTERY(0x34),
        /** System fault. */
        C2PE_SYSTEM_FAULT(0x48), // prima occorrenza, poi 4b
        /** False code entered. */
        C2PE_FALSE_CODE(0x54),
        /** Cyclical test call. */
        C2PE_CYCLICAL_TEST_CALL(0x52);

        private int offset;

        Event(int offset)
        {
            this.offset = offset;
        }

        /**
         * Returns the byte offset of this event's code within the C200B payload.
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

    private Map<Event,Byte> eventCodes;
    private byte[]          inputCodes;

    /**
     * Creates a new C200B configuration.
     *
     * @param eventCodes the C200B code to report for each event
     * @param inputCodes the C200B code for each logical input, {@code 0xff} where no input exists, length {@link ElkrommFacade#MAX_LOGICAL_INPUTS}
     */
    public C200bParameters(Map<Event,Byte> eventCodes, byte[] inputCodes) {
        setEventCodes(eventCodes);
        setInputCodes(inputCodes);
    }

    /**
     * Returns the configured event codes.
     *
     * @return the event-to-code map
     */
    public Map<Event,Byte> getEventCodes() {
        return eventCodes;
    }

    /**
     * Replaces the full set of event codes.
     *
     * @param eventCodes the event-to-code map to set, not {@code null}
     * @throws IllegalArgumentException if {@code eventCodes} is {@code null}
     */
    public void setEventCodes(Map<Event,Byte> eventCodes) {
        if (eventCodes == null) throw new IllegalArgumentException("Missing mandatory event codes");
        
        this.eventCodes = new HashMap<>();

        for (Entry<Event,Byte> pivot : eventCodes.entrySet()) {
            this.eventCodes.put(pivot.getKey(), pivot.getValue().byteValue());
        }
    }

    /**
     * Sets the code for a single event.
     *
     * @param event the event to set
     * @param code the C200B code to report for it
     */
    public void setEventCode(Event event, byte code) {
        this.eventCodes.put(event, code);
    }

    /**
     * Returns a copy of the per-input C200B codes.
     *
     * @return the input codes, length {@link ElkrommFacade#MAX_LOGICAL_INPUTS}
     */
    public byte[] getInputCodes() {
        return Arrays.copyOf(inputCodes, ElkrommFacade.MAX_LOGICAL_INPUTS);
    }

    /**
     * Sets the per-input C200B codes, padding any missing trailing entries with {@code 0xff}.
     *
     * @param inputCodes the input codes to set, not {@code null}
     * @throws IllegalArgumentException if {@code inputCodes} is {@code null}
     */
    public void setInputCodes(byte[] inputCodes) {
        if (inputCodes == null) throw new IllegalArgumentException("Missing mandatory input codes");

        this.inputCodes = new byte[ElkrommFacade.MAX_LOGICAL_INPUTS];
        Arrays.fill(this.inputCodes, (byte)0xff);
        System.arraycopy(inputCodes, 0, this.inputCodes, 0, Math.min(inputCodes.length, this.inputCodes.length));
    }

    /**
     * Sets the C200B code for a single input.
     *
     * @param index 0-based input index
     * @param code the C200B code to set
     * @throws IllegalArgumentException if {@code index} is out of range
     */
    public void setInputCode(int index, byte code) {
        if (index < 0 || index >= this.inputCodes.length) throw new IllegalArgumentException("Wrong index " + index + ", expected value between 0 and " + (this.inputCodes.length - 1));

        this.inputCodes[index] = code;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{eventCodes=").append(eventCodes).append(", inputCodes=").append(Arrays.toString(inputCodes)).append("}");

        return sb.toString();
    }
}
