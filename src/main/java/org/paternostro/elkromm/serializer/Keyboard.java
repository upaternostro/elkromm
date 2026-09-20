package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input;

/**
 * {@link org.paternostro.elkromm.dto.Keyboard} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset (relative to the keyboard)</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Address</td><td>Keypad address (base 1)</td></tr>
 *  <tr><td>1</td><td>?</td></tr>
 *  <tr><td>2-5</td><td>Version</td><td>ASCII</td></tr>
 *  <tr><td>6-43</td><td>First onboard input</td><td>38 bytes, see {@link Input}</td></tr>
 *  <tr><td>44-81</td><td>Second onboard input</td><td>38 bytes, see {@link Input}</td></tr>
 *  <tr><td>82</td><td>Enablings bitmask</td><td>GONG, ENTRY, EXIT, MASKING, FIRE, PANIC, HELP</td></tr>
 *  <tr><td>83</td><td>Bitmask of associated partitions</td><td>LSB = partition 1</td></tr>
 *  <tr><td>84</td><td>Audio feature bitmask</td><td>CAPABLE, ENABLED</td></tr>
 *  <tr><td>85-108</td><td>Keypad name</td></tr>
 *  <tr><td>109</td><td>?</td><td>Must be excluded from the block checksum calculation (see {@link Input} and the note in {@link Expansions}) — confirmed on real hardware</td></tr>
 *  <tr><td>110</td><td>?</td><td>Always observed as {@code 0x00} in the available captures; the code zeroes it anyway for symmetry with the {@link Expansions} code, but it doesn't appear necessary</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Input
 * @usedby {@link Keyboards}
 * @usedby {@link SingleKeyboard}
 */
public class Keyboard implements ElkrommSerializer<org.paternostro.elkromm.dto.Keyboard>
{
    public static final int PAYLOAD_SIZE = 111;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Keyboard obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        data[0] = (byte)(obj.getAddress() & 0xFF);
        ElkrommUtils.setText(data, 2, obj.getVersion(), 4);
        System.arraycopy(iSerializer.serialize(obj.getFirstInput()), 0, data, 6, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        System.arraycopy(iSerializer.serialize(obj.getSecondInput()), 0, data, 6 + org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        data[6 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE] = obj.getEnablings();
        data[7 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        data[8 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE] = obj.getAudioFeatures();
        ElkrommUtils.setText(data, 9 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE, obj.getName(), ElkrommFacade.NAME_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Keyboard deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE];
        Input                       input1;
        Input                       input2;

        System.arraycopy(data, 6, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        input1 = iSerializer.deserialize(iData);

        System.arraycopy(data, 6 + org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        input2 = iSerializer.deserialize(iData);

        return new org.paternostro.elkromm.dto.Keyboard(data[0], ElkrommUtils.getText(data, 2, 4), input1, input2, data[6 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE], ElkrommUtils.unpackPartitions(data[7 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE]), data[8 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE], ElkrommUtils.getText(data, 9 + 2*org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE, ElkrommFacade.NAME_LENGTH));
    }
}
