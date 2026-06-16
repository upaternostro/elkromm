package org.paternostro.elkromm;

public enum ElkronCommand {
    HELLO(0x60),
    LOGIN(0x49),
    SEND(0x65),
    LOGOUT(0x63),
    PARTITIONS_AND_AREAS(0x55),
    SYSTEM_STATUS(0x80),
    ARM_DISARM_SECTOR(0x81),
    INPUT_STATUS(0x84),
    EXPANSIONS(0x51),
    EXCLUDE_INCLUDE_INPUT(0x83),
    PERIPHERAL_UNITS_ADDRESSES(0x62),
    CHECKSUM(0x50),
    USERS(0x5b),
    KEYS(0x5c),
    PARAMETERS_ENABLINGS(0x26),
    USER_ENABLINGS(0x87),
    PHONE_NUMBERS(0x57),
    PHONE_PARAMETERS(0x56),
    PSTN_GSM(0x5a),
    SMS(0x59),
    C200B(0x58),
    TIME_PROGRAMMER(0x54),
    ENABLE_DISABLE_USER(0x88),
    KEY_STATUS(0x8b),

    EVENT_LOG(0x70),    
    
    USER_PROGRAMMING(0x95),
    SET_PARAMETERS_ENABLINGS(0x96),

    SET_TIME_PROGRAMMER(0xe4),
    SET_PARTITIONS_AND_AREAS(0xe5),
    SET_PHONE_PARAMETERS(0xe6),
    SET_PHONE_NUMBERS(0xe7),
    SET_C200B(0xe8),
    SET_SMS(0xe9),
    SET_PSTN_GSM(0xea),
    SET_USERS(0xeb),
    SET_KEYS(0xec),

    // No checksum!
    SMS_PROGRAMMING(0xa0);

    protected int value;

    ElkronCommand(int value) {
        this.value = value & 0xFF;
    }

    public int getValue() {
        return value;
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
