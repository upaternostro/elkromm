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
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0-4</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>5, 7, 9</td><td>Burglar time</td><td>{@link Time}: 0=30s, 1=60s, 2=90s, 3=180s, 4=9min. <b>All three offsets always hold the same value</b> ({@code data[5]=data[7]=data[9]})</td></tr>
 *  <tr><td>6</td><td>Pre-alarm time</td><td>{@link Time}, same values</td></tr>
 *  <tr><td>8</td><td>Emergency time</td><td>{@link Time}, same values</td></tr>
 *  <tr><td>10</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>11</td><td>Power lack</td><td>{@link PowerLack}: 0=1h, 1=2h, 2=4h</td></tr>
 *  <tr><td>12</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>13</td><td>Alarm count</td><td>{@link AlarmCount}: 0=none, 1=two, 2=four, 3=six, 4=eight</td></tr>
 *  <tr><td>14</td><td>Notice</td><td>{@link Notice}: 0=none, 5=5min, 10=10min, 15=15min, 20=20min</td></tr>
 *  <tr><td>15</td><td>Time programmer</td><td>{@link Enabling}: 0=disabled, 1=enabled</td></tr>
 *  <tr><td>16</td><td>DST</td><td>Bitmask: bit0=enabled, bit1=last Sunday (instead of first)</td></tr>
 *  <tr><td>17-20</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>21</td><td>DST OFF month</td><td>{@link Month}: 1-12</td></tr>
 *  <tr><td>22</td><td>DST ON month</td><td>{@link Month}: 1-12</td></tr>
 *  <tr><td>23</td><td>LAN</td><td>{@link Enabling}: 0=disabled, 1=enabled</td></tr>
 *  <tr><td>24</td><td>Play</td><td>Bitmask: bit0=fault, bit1=partitions, bit2=system, bit3=service</td></tr>
 *  <tr><td>25</td><td>Help</td><td>Bit 7 ({@code 0x80})=enabled, bits 0-2=keypad address - 1</td></tr>
 *  <tr><td>26-29</td><td>Block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ParametersEnablings implements ElkrommSerializer<org.paternostro.elkromm.dto.ParametersEnablings>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.ParametersEnablings obj) {
        byte[]  data = new byte[length()];

        // byte 5, 7 and 9 contain the same value
        data[ 5] = 
        data[ 7] = 
        data[ 9] = obj.getBulgarTime().getValue();
        data[ 6] = obj.getPreAlarmTime().getValue();
        data[ 8] = obj.getEmergencyTime().getValue();
        data[11] = obj.getPowerLack().getValue();
        data[13] = obj.getAlarmCount().getValue();
        data[14] = obj.getNotice().getValue();
        data[15] = obj.getTimeProgrammer().getValue();
        data[16] = obj.getDST();
        data[17] = data[18] = data[19] = data[20] = 0x55; // sembra non essere il plant code (testato con Hi-Connect ed il simulatore, cambiando plant code qui arriva sempre 0x55555555)
        data[21] = obj.getOff().getValue();
        data[22] = obj.getOn().getValue();
        data[23] = obj.getLan().getValue();
        data[24] = obj.getPlay();
        data[25] = obj.getHelp();
        
        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.ParametersEnablings deserialize(byte[] data) {
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        return new org.paternostro.elkromm.dto.ParametersEnablings(Time.valueOf(data[5]), Time.valueOf(data[8]), Time.valueOf(data[6]), AlarmCount.valueOf(data[13]), PowerLack.valueOf(data[11]), data[24], data[25], Enabling.valueOf(data[23]), Enabling.valueOf(data[15]), Notice.valueOf(data[14]), data[16], Month.valueOf(data[22]), Month.valueOf(data[21]));
    }

    @Override
    public int length() {
        return SerializersConstants.PARAMETERS_ENABLINGS_SIZE;
    }
}
