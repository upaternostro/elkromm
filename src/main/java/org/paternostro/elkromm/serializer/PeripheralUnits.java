package org.paternostro.elkromm.serializer;

/**
 * {@link org.paternostro.elkromm.dto.PeripheralUnits} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Keyboards number</td><td>Number of keyboards connected to the system, {@code 0} if none</td></tr>
 *  <tr><td>1 - k</td><td>Keyboard address</td><td>Address of the &lt;K&gt;th keyboard</td></tr>
 *  <tr><td>k+1</td><td>Readers number</td><td>Number of readers connected to the system, {@code 0} if none</td></tr>
 *  <tr><td>k+2 - k+r+1</td><td>Reader address</td><td>Address of the &lt;R&gt;th keyboard</td></tr>
 *  <tr><td>k+r+2</td><td>Expansions number</td><td>Number of expansions connected to the system, {@code 0} if none</td></tr>
 *  <tr><td>k+r+3 - k+r+e+2</td><td>Expansion address</td><td>Address of the &lt;E&gt;th keyboard</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PeripheralUnits implements ElkrommSerializer<org.paternostro.elkromm.dto.PeripheralUnits>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.PeripheralUnits obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]  data = new byte[3 + obj.getKeypadNum() + obj.getReaderNum() + obj.getExpansionNum()]; // 3 byte di header + 1 byte per ogni unità periferica
        int     pivot = 0;

        data[pivot++] = (byte)(obj.getKeypadNum() & 0xFF); // tastiere

        for (int i = 0; i < obj.getKeypadNum(); i++) {
            data[pivot++] = (byte)(obj.getKeypadAddress(i) & 0xFF); // indirizzo tastiera i-esima
        }

        data[pivot++] = (byte)(obj.getReaderNum() & 0xFF); // inseritori

        for (int i = 0; i < obj.getReaderNum(); i++) {
            data[pivot++] = (byte)(obj.getReaderAddress(i) & 0xFF); // indirizzo inseritore i-esimo
        }

        data[pivot++] = (byte)(obj.getExpansionNum() & 0xFF); // espansioni

        for (int i = 0; i < obj.getExpansionNum(); i++) {
            data[pivot++] = (byte)(obj.getExpansionAddress(i) & 0xFF); // indirizzo espansione i-esima
        }

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.PeripheralUnits deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length < 3) throw new IllegalArgumentException("Wrong data size");

        org.paternostro.elkromm.dto.PeripheralUnits retval = new org.paternostro.elkromm.dto.PeripheralUnits();
        int                                         pivot = 0;

        for (int i = 0; i < data[pivot]; i++) {
            retval.addKeypad(data[pivot + i + 1]);
        }

        pivot += data[pivot] + 1;

        for (int i = 0; i < data[pivot]; i++) {
            retval.addReader(data[pivot + i + 1]);
        }

        pivot += data[pivot] + 1;

        for (int i = 0; i < data[pivot]; i++) {
            retval.addExpansion(data[pivot + i + 1]);
        }

        return retval;
    }

    @Override
    public int length() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'length'");
    }
}
