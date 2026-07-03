package org.paternostro.elkromm.dto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.paternostro.elkromm.ElkrommFacade;

public class C200bParameters {
    public enum Event {
        C2PE_BURGLAR_ALARM(0x38), // prima occorrenza, poi 39, 3a, 3b
        C2PE_PRE_ALARM(0x3c),
        C2PE_TECHNOLOGICAL_ALARM_TYPE_1(0x58),
        C2PE_TECHNOLOGICAL_ALARM_TYPE_2(0x59),
        C2PE_TECHNOLOGICAL_ALARM_TYPE_3(0x5a),
        C2PE_FIRE_ALARM(0x43),
        C2PE_PANIC(0x41),
        C2PE_SILENT_PANIC(0x42),
        C2PE_MEDICAL_EMERGENCY(0x47),
        C2PE_HOLD_UP(0x4e),
        C2PE_SYSTEM_ON_OFF(0x4c), // prima occorrenza, poi 4d, 4f
        C2PE_PARTITIONS_ON_OFF(0x4a),
        C2PE_MAINTENANCE(0x53),
        C2PE_INPUT_INCLUSION_EXCLUSION(0x50), // prima occorrenza, poi 51
        C2PE_TAMPERING(0x32), // prima occorrenza, poi 40, 5b
        C2PE_MAINS_POWER(0x35),
        C2PE_LOW_BATTERY(0x34),
        C2PE_SYSTEM_FAULT(0x48), // prima occorrenza, poi 4b
        C2PE_FALSE_CODE(0x54),
        C2PE_CYCLICAL_TEST_CALL(0x52);

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

    private Map<Event,Byte> eventCodes;
    private byte[]          inputCodes;

    public C200bParameters(Map<Event,Byte> eventCodes, byte[] inputCodes) {
        setEventCodes(eventCodes);
        setInputCodes(inputCodes);
    }

    public Map<Event,Byte> getEventCodes() {
        return eventCodes;
    }

    public void setEventCodes(Map<Event,Byte> eventCodes) {
        if (eventCodes == null) throw new IllegalArgumentException("Missing mandatory event codes");
        
        this.eventCodes = new HashMap<>();

        for (Entry<Event,Byte> pivot : eventCodes.entrySet()) {
            this.eventCodes.put(pivot.getKey(), pivot.getValue().byteValue());
        }
    }

    public void setEventCode(Event event, byte code) {
        this.eventCodes.put(event, code);
    }

    public byte[] getInputCodes() {
        return Arrays.copyOf(inputCodes, ElkrommFacade.MAX_LOGICAL_INPUTS);
    }

    public void setInputCodes(byte[] inputCodes) {
        if (inputCodes == null) throw new IllegalArgumentException("Missing mandatory input codes");

        this.inputCodes = new byte[ElkrommFacade.MAX_LOGICAL_INPUTS];
        Arrays.fill(this.inputCodes, (byte)0xff);
        System.arraycopy(inputCodes, 0, this.inputCodes, 0, Math.min(inputCodes.length, this.inputCodes.length));
    }

    public void setInputCode(int index, byte code) {
        this.inputCodes[index] = code;
    }
}
