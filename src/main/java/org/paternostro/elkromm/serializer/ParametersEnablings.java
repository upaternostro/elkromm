package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.ParametersEnablings.AlarmCount;
import org.paternostro.elkromm.dto.ParametersEnablings.Enabling;
import org.paternostro.elkromm.dto.ParametersEnablings.Month;
import org.paternostro.elkromm.dto.ParametersEnablings.Notice;
import org.paternostro.elkromm.dto.ParametersEnablings.PowerLack;
import org.paternostro.elkromm.dto.ParametersEnablings.Time;

/**
 * {@link org.paternostro.elkromm.dto.ParametersEnablings} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00-0x04</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x05, 0x07, 0x09</td><td>Burglar time</td><td>{@link Time}: 0=30s, 1=60s, 2=90s, 3=180s, 4=9min. <b>All three offsets always hold the same value</b> ({@code data[5]=data[7]=data[9]})</td><td>{@link #BURGLAR_TIME_OFFSET}, {@link #BURGLAR_TIME_MIRROR_1_OFFSET}, {@link #BURGLAR_TIME_MIRROR_2_OFFSET}</td></tr>
 *  <tr><td>0x06</td><td>Pre-alarm time</td><td>{@link Time}, same values</td><td>{@link #PRE_ALARM_TIME_OFFSET}</td></tr>
 *  <tr><td>0x08</td><td>Emergency time</td><td>{@link Time}, same values</td><td>{@link #EMERGENCY_TIME_OFFSET}</td></tr>
 *  <tr><td>0x0a</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x0b</td><td>Power lack</td><td>{@link PowerLack}: 0=1h, 1=2h, 2=4h</td><td>{@link #POWER_LACK_OFFSET}</td></tr>
 *  <tr><td>0x0c</td><td>?</td><td>Not mapped by any DTO field</td><td></td></tr>
 *  <tr><td>0x0d</td><td>Alarm count</td><td>{@link AlarmCount}: 0=none, 1=two, 2=four, 3=six, 4=eight</td><td>{@link #ALARM_COUNT_OFFSET}</td></tr>
 *  <tr><td>0x0e</td><td>Notice</td><td>{@link Notice}: 0=none, 5=5min, 10=10min, 15=15min, 20=20min</td><td>{@link #NOTICE_OFFSET}</td></tr>
 *  <tr><td>0x0f</td><td>Time programmer</td><td>{@link Enabling}: 0=disabled, 1=enabled</td><td>{@link #TIME_PROGRAMMER_OFFSET}</td></tr>
 *  <tr><td>0x10</td><td>DST</td><td>Bitmask: bit0=enabled, bit1=last Sunday (instead of first)</td><td>{@link #DST_OFFSET}</td></tr>
 *  <tr><td>0x11-0x14</td><td>?</td><td>Not mapped by any DTO field</td><td>{@link #UNKNOWN_OFFSET}</td></tr>
 *  <tr><td>0x15</td><td>DST OFF month</td><td>{@link Month}: 1-12</td><td>{@link #DST_OFF_MONTH_OFFSET}</td></tr>
 *  <tr><td>0x16</td><td>DST ON month</td><td>{@link Month}: 1-12</td><td>{@link #DST_ON_MONTH_OFFSET}</td></tr>
 *  <tr><td>0x17</td><td>LAN</td><td>{@link Enabling}: 0=disabled, 1=enabled</td><td>{@link #LAN_OFFSET}</td></tr>
 *  <tr><td>0x18</td><td>Play</td><td>Bitmask: bit0=fault, bit1=partitions, bit2=system, bit3=service</td><td>{@link #PLAY_OFFSET}</td></tr>
 *  <tr><td>0x19</td><td>Help</td><td>Bit 7 ({@code 0x80})=enabled, bits 0-2=keypad address - 1</td><td>{@link #HELP_OFFSET}</td></tr>
 *  <tr><td>0x1a-0x1d</td><td>Block checksum</td><td></td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ParametersEnablings implements ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings>
{
    /** Payload size */
    public static final int PAYLOAD_SIZE = 30;

    /** Offset of the burglar time */
    public static final int BURGLAR_TIME_OFFSET          = 0x05;

    /** Offset of a copy of the burglar time, always holding the same value */
    public static final int BURGLAR_TIME_MIRROR_1_OFFSET = 0x07;

    /** Offset of a second copy of the burglar time, always holding the same value */
    public static final int BURGLAR_TIME_MIRROR_2_OFFSET = 0x09;

    /** Offset of the pre-alarm time */
    public static final int PRE_ALARM_TIME_OFFSET        = 0x06;

    /** Offset of the emergency time */
    public static final int EMERGENCY_TIME_OFFSET        = 0x08;

    /** Offset of the power lack time */
    public static final int POWER_LACK_OFFSET            = 0x0b;

    /** Offset of the alarm count */
    public static final int ALARM_COUNT_OFFSET           = 0x0d;

    /** Offset of the notice time */
    public static final int NOTICE_OFFSET                = 0x0e;

    /** Offset of the time programmer */
    public static final int TIME_PROGRAMMER_OFFSET       = 0x0f;

    /** Offset of the DST bitmask */
    public static final int DST_OFFSET                   = 0x10;

    /** Offset of four bytes of unknown meaning, always written as {@code 0x55} */
    public static final int UNKNOWN_OFFSET               = 0x11;

    /** Offset of the DST OFF month */
    public static final int DST_OFF_MONTH_OFFSET         = 0x15;

    /** Offset of the DST ON month */
    public static final int DST_ON_MONTH_OFFSET          = 0x16;

    /** Offset of the LAN enabling */
    public static final int LAN_OFFSET                   = 0x17;

    /** Offset of the play bitmask */
    public static final int PLAY_OFFSET                  = 0x18;

    /** Offset of the help flags and keypad address */
    public static final int HELP_OFFSET                  = 0x19;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.ParametersEnablings obj) {
        byte[]  data = new byte[PAYLOAD_SIZE];

        // BURGLAR_TIME_OFFSET and its two mirrors contain the same value
        data[BURGLAR_TIME_OFFSET] =
        data[BURGLAR_TIME_MIRROR_1_OFFSET] =
        data[BURGLAR_TIME_MIRROR_2_OFFSET] = obj.getBulgarTime().getValue();
        data[PRE_ALARM_TIME_OFFSET] = obj.getPreAlarmTime().getValue();
        data[EMERGENCY_TIME_OFFSET] = obj.getEmergencyTime().getValue();
        data[POWER_LACK_OFFSET] = obj.getPowerLack().getValue();
        data[ALARM_COUNT_OFFSET] = obj.getAlarmCount().getValue();
        data[NOTICE_OFFSET] = obj.getNotice().getValue();
        data[TIME_PROGRAMMER_OFFSET] = obj.getTimeProgrammer().getValue();
        data[DST_OFFSET] = obj.getDST();
        data[UNKNOWN_OFFSET] = data[UNKNOWN_OFFSET + 1] = data[UNKNOWN_OFFSET + 2] = data[UNKNOWN_OFFSET + 3] = 0x55; // sembra non essere il plant code (testato con Hi-Connect ed il simulatore, cambiando plant code qui arriva sempre 0x55555555)
        data[DST_OFF_MONTH_OFFSET] = obj.getOff().getValue();
        data[DST_ON_MONTH_OFFSET] = obj.getOn().getValue();
        data[LAN_OFFSET] = obj.getLan().getValue();
        data[PLAY_OFFSET] = obj.getPlay();
        data[HELP_OFFSET] = obj.getHelp();
        
        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.ParametersEnablings deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getBlockChecksum(data));

        return new org.paternostro.elkromm.dto.ParametersEnablings(Time.valueOf(data[BURGLAR_TIME_OFFSET]), Time.valueOf(data[EMERGENCY_TIME_OFFSET]), Time.valueOf(data[PRE_ALARM_TIME_OFFSET]), AlarmCount.valueOf(data[ALARM_COUNT_OFFSET]), PowerLack.valueOf(data[POWER_LACK_OFFSET]), data[PLAY_OFFSET], data[HELP_OFFSET], Enabling.valueOf(data[LAN_OFFSET]), Enabling.valueOf(data[TIME_PROGRAMMER_OFFSET]), Notice.valueOf(data[NOTICE_OFFSET]), data[DST_OFFSET], Month.valueOf(data[DST_ON_MONTH_OFFSET]), Month.valueOf(data[DST_OFF_MONTH_OFFSET]));
    }
}
