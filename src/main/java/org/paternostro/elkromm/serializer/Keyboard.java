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
 *  <tr><th>Offset (relative to the keyboard)</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Address</td><td>Keypad address (base 1)</td><td>{@link #ADDRESS_OFFSET}</td></tr>
 *  <tr><td>0x01</td><td>?</td><td></td><td></td></tr>
 *  <tr><td>0x02-0x05</td><td>Version</td><td>ASCII</td><td>{@link #VERSION_OFFSET}</td></tr>
 *  <tr><td>0x06-0x2b</td><td>First onboard input</td><td>38 bytes, see {@link Input}</td><td>{@link #FIRST_INPUT_OFFSET}</td></tr>
 *  <tr><td>0x2c-0x51</td><td>Second onboard input</td><td>38 bytes, see {@link Input}</td><td>{@link #SECOND_INPUT_OFFSET}</td></tr>
 *  <tr><td>0x52</td><td>Enablings bitmask</td><td>GONG, ENTRY, EXIT, MASKING, FIRE, PANIC, HELP</td><td>{@link #ENABLINGS_OFFSET}</td></tr>
 *  <tr><td>0x53</td><td>Bitmask of associated partitions</td><td>LSB = partition 1</td><td>{@link #ASSOCIATED_PARTITIONS_OFFSET}</td></tr>
 *  <tr><td>0x54</td><td>Audio feature bitmask</td><td>CAPABLE, ENABLED</td><td>{@link #AUDIO_FEATURES_OFFSET}</td></tr>
 *  <tr><td>0x55-0x6c</td><td>Keypad name</td><td></td><td>{@link #NAME_OFFSET}</td></tr>
 *  <tr><td>0x6d</td><td>?</td><td>Must be excluded from the block checksum calculation (see {@link Input} and the note in {@link Expansions}) — confirmed on real hardware</td><td>{@link #PSEUDO_CHECKSUM_OFFSET}</td></tr>
 *  <tr><td>0x6e</td><td>?</td><td>Always observed as {@code 0x00} in the available captures; the code zeroes it anyway for symmetry with the {@link Expansions} code, but it doesn't appear necessary</td><td></td></tr>
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
    /** Payload size */
    public static final int PAYLOAD_SIZE = 111;

    /** Offset of the keypad address */
    public static final int ADDRESS_OFFSET               = 0x00;

    /** Offset of the firmware version */
    public static final int VERSION_OFFSET               = 0x02;

    /** Offset of the first onboard input */
    public static final int FIRST_INPUT_OFFSET           = 0x06;

    /** Offset of the second onboard input */
    public static final int SECOND_INPUT_OFFSET          = FIRST_INPUT_OFFSET + org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE;

    /** Offset of the enablings bitmask */
    public static final int ENABLINGS_OFFSET             = SECOND_INPUT_OFFSET + org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE;

    /** Offset of the bitmask of associated partitions */
    public static final int ASSOCIATED_PARTITIONS_OFFSET = ENABLINGS_OFFSET + 1;

    /** Offset of the audio feature bitmask */
    public static final int AUDIO_FEATURES_OFFSET        = ASSOCIATED_PARTITIONS_OFFSET + 1;

    /** Offset of the keypad name */
    public static final int NAME_OFFSET                  = AUDIO_FEATURES_OFFSET + 1;

    /** Offset of the pseudo checksum, excluded from the block checksum calculation */
    public static final int PSEUDO_CHECKSUM_OFFSET       = NAME_OFFSET + ElkrommFacade.NAME_LENGTH;

    /** Size of the pseudo checksum */
    public static final int PSEUDO_CHECKSUM_SIZE         = 2;

    /** Number of onboard inputs */
    public static final int ONBOARD_INPUTS               = 2;

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Keyboard obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                      data = new byte[PAYLOAD_SIZE];
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        data[ADDRESS_OFFSET] = (byte)(obj.getAddress() & 0xFF);
        ElkrommUtils.setText(data, VERSION_OFFSET, obj.getVersion(), ElkrommFacade.VERSION_LENGTH);
        System.arraycopy(iSerializer.serialize(obj.getFirstInput()), 0, data, FIRST_INPUT_OFFSET, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        System.arraycopy(iSerializer.serialize(obj.getSecondInput()), 0, data, SECOND_INPUT_OFFSET, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        data[ENABLINGS_OFFSET] = obj.getEnablings();
        data[ASSOCIATED_PARTITIONS_OFFSET] = ElkrommUtils.packPartitions(obj.getAssociatedPartitions());
        data[AUDIO_FEATURES_OFFSET] = obj.getAudioFeatures();
        ElkrommUtils.setText(data, NAME_OFFSET, obj.getName(), ElkrommFacade.NAME_LENGTH);

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

        System.arraycopy(data, FIRST_INPUT_OFFSET, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        input1 = iSerializer.deserialize(iData);

        System.arraycopy(data, SECOND_INPUT_OFFSET, iData, 0, org.paternostro.elkromm.serializer.Input.PAYLOAD_SIZE);
        input2 = iSerializer.deserialize(iData);

        return new org.paternostro.elkromm.dto.Keyboard(data[ADDRESS_OFFSET], ElkrommUtils.getText(data, VERSION_OFFSET, ElkrommFacade.VERSION_LENGTH), input1, input2, data[ENABLINGS_OFFSET], ElkrommUtils.unpackPartitions(data[ASSOCIATED_PARTITIONS_OFFSET]), data[AUDIO_FEATURES_OFFSET], ElkrommUtils.getText(data, NAME_OFFSET, ElkrommFacade.NAME_LENGTH));
    }
}
