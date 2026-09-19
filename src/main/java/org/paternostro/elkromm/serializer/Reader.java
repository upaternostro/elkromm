package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Input;

/**
 * {@link org.paternostro.elkromm.dto.Reader} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Bus address</td><td>Also identifies the reader in single-instance writes (there's no separate {@code SingleReader} wrapper)</td></tr>
 *  <tr><td>1-5</td><td>?</td><td>Not mapped by any DTO field</td></tr>
 *  <tr><td>6-43</td><td>First onboard input</td><td>38 bytes, see {@link org.paternostro.elkromm.serializer.Input}</td></tr>
 *  <tr><td>44-81</td><td>Second onboard input</td><td>38 bytes, see {@link org.paternostro.elkromm.serializer.Input}</td></tr>
 *  <tr><td>82</td><td>LED 1</td><td>Associated partition ({@link ElkrommFacade.Partition}), {@code 0x00} = unused</td></tr>
 *  <tr><td>83</td><td>LED 2</td><td>Associated partition</td></tr>
 *  <tr><td>84</td><td>LED 3</td><td>Associated partition</td></tr>
 *  <tr><td>85</td><td>LED 4</td><td>Associated partition</td></tr>
 *  <tr><td>86</td><td>Enablings bitmask</td><td>{@link org.paternostro.elkromm.dto.Reader.Enablings}: only {@code MASKING} (0x01) known</td></tr>
 *  <tr><td>87-110</td><td>Name</td><td>24 bytes</td></tr>
 *  <tr><td>111-112</td><td>?</td><td>Not mapped by any DTO field. Zeroed on write by {@link Readers} in analogy with {@link Expansions}/{@link Keyboards}, <b>unverified on real hardware</b> (the author owns no physical readers) — for this reason the block checksum remains a warning, not an exception, on this structure</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 * 
 * @see Input
 * @usedby {@link Readers}
 */
public class Reader implements ElkrommSerializer<org.paternostro.elkromm.dto.Reader>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.Reader obj)
    {
        if (obj == null) throw new IllegalArgumentException("Missing mandatory obj");

        byte[]                      data = new byte[length()];
        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();

        data[0] = (byte)(obj.getAddress() & 0xFF);
        System.arraycopy(iSerializer.serialize(obj.getFirstInput()), 0, data, 6, SerializersConstants.INPUT_SIZE);
        System.arraycopy(iSerializer.serialize(obj.getSecondInput()), 0, data, 6 + SerializersConstants.INPUT_SIZE, SerializersConstants.INPUT_SIZE);
        data[6 + 2*SerializersConstants.INPUT_SIZE] = obj.getLed1() == null ? 0 : obj.getLed1().getValue();
        data[7 + 2*SerializersConstants.INPUT_SIZE] = obj.getLed2() == null ? 0 : obj.getLed2().getValue();
        data[8 + 2*SerializersConstants.INPUT_SIZE] = obj.getLed3() == null ? 0 : obj.getLed3().getValue();
        data[9 + 2*SerializersConstants.INPUT_SIZE] = obj.getLed4() == null ? 0 : obj.getLed4().getValue();
        data[10 + 2*SerializersConstants.INPUT_SIZE] = obj.getEnablings();
        ElkrommUtils.setText(data, 11 + 2*SerializersConstants.INPUT_SIZE, obj.getName(), ElkrommFacade.NAME_LENGTH);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.Reader deserialize(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length == 0) throw new IllegalArgumentException("Empty mandatory data");
        if (data.length != length()) throw new IllegalArgumentException("Wrong data size");

        ElkrommSerializer<Input>    iSerializer = ElkrommFactory.getFactory().getInputSerializer();
        byte[]                      iData = new byte[SerializersConstants.INPUT_SIZE];
        Input                       input1;
        Input                       input2;

        System.arraycopy(data, 6, iData, 0, SerializersConstants.INPUT_SIZE);
        input1 = iSerializer.deserialize(iData);

        System.arraycopy(data, 6 + SerializersConstants.INPUT_SIZE, iData, 0, SerializersConstants.INPUT_SIZE);
        input2 = iSerializer.deserialize(iData);

        return new org.paternostro.elkromm.dto.Reader(data[0], input1, input2, ElkrommFacade.Partition.valueOf(data[6 + 2*SerializersConstants.INPUT_SIZE]), ElkrommFacade.Partition.valueOf(data[7 + 2*SerializersConstants.INPUT_SIZE]), ElkrommFacade.Partition.valueOf(data[8 + 2*SerializersConstants.INPUT_SIZE]), ElkrommFacade.Partition.valueOf(data[9 + 2*SerializersConstants.INPUT_SIZE]), data[10 + 2*SerializersConstants.INPUT_SIZE], ElkrommUtils.getText(data, 11 + 2*SerializersConstants.INPUT_SIZE, ElkrommFacade.NAME_LENGTH));
    }

    @Override
    public int length()
    {
        return SerializersConstants.READER_SIZE;
    }
}
