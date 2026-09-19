package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.Partition;

/**
 * {@link org.paternostro.elkromm.dto.AreasAndPartitions} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th></tr>
 *  <tr><td>0</td><td>Number of areas</td><td>Can be zero if unused</td></tr>
 *  <tr><td>1-4</td><td>Bitmask of partitions assigned to the area</td><td>LSB = partition 1, one byte per area</td></tr>
 *  <tr><td>5-28</td><td>First area name</td></tr>
 *  <tr><td>29-52</td><td>Second area name</td></tr>
 *  <tr><td>53-76</td><td>Third area name</td></tr>
 *  <tr><td>77-100</td><td>Fourth area name</td></tr>
 *  <tr><td>101</td><td>Number of partitions</td></tr>
 *  <tr><td>102</td><td>Self-exclusion bitmask</td><td>LSB = partition 1. A partition cannot be both self-exclusion and arming block. Standard partitions have both flags to zero.</td></tr>
 *  <tr><td>103</td><td>Arming block bitmask</td><td>LSB = partition 1</td></tr>
 *  <tr><td>104-119</td><td>Entry delay</td><td>2 bytes per partition (starting at 104-105 for partition 1 and so on), big endian</td></tr>
 *  <tr><td>120-135</td><td>Exit delay</td><td>2 bytes per partition (starting at 120-121 for partition 1 and so on), big endian</td></tr>
 *  <tr><td>136-159</td><td>First partition name</td></tr>
 *  <tr><td>160-183</td><td>Second partition name</td></tr>
 *  <tr><td>184-207</td><td>Third partition name</td></tr>
 *  <tr><td>208-231</td><td>Fourth partition name</td></tr>
 *  <tr><td>232-255</td><td>Fifth partition name</td></tr>
 *  <tr><td>256-279</td><td>Sixth partition name</td></tr>
 *  <tr><td>280-303</td><td>Seventh partition name</td></tr>
 *  <tr><td>304-327</td><td>Eighth partition name</td></tr>
 *  <tr><td>328</td><td>?</td><td>The serializer explicitly zeroes it on write ({@code data[328] = 0; // ???}); same signature as the other "status" bytes already documented (see {@link Input}) — suspected dynamic content not handled by the client, not yet identified but computed in checksum</td></tr>
 *  <tr><td>329-332</td><td>Block checksum</td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class AreasAndPartitions implements ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.AreasAndPartitions obj) {
        byte[]  data = new byte[length()];

        data[0] = (byte)obj.getAreaNum(); // numero di aree

        for (int i = 0; i < ElkrommFacade.MAX_AREAS; i++) {
            data[i + 1] = (byte)ElkrommUtils.packPartitions(obj.getArea(i).getAssociatedPartitions()); // settori nell'area i-esima (bit mask)
            ElkrommUtils.setText(data, 5 + i*24, obj.getArea(i).getName(), 24); // nome dell'area i-esima
        }
        
        data[101] = (byte)obj.getPartitionNum(); // numero di settori
        data[102] = 0; // self exclusion (bit mask)
        data[103] = 0; // arming block (bit mask)

        for (int i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            switch (obj.getPartition(i).getType()) { // tipo del settore i-esimo
                case STANDARD:
                    break;
                case SELF_EXCLUSION:
                    data[102] |= ElkrommFacade.Partition.values()[i].getValue();
                    break;
                case ARMING_BLOCK:
                    data[103] |= ElkrommFacade.Partition.values()[i].getValue();
                    break;
                case UNKNOWN:
                    break;
            }

            ElkrommUtils.setWord(data, 104 + i*2, obj.getPartition(i).getEntryDelay()); // tempo di ingresso
            ElkrommUtils.setWord(data, 120 + i*2, obj.getPartition(i).getExitDelay()); // tempo di uscita
            ElkrommUtils.setText(data, 136 + i*24, obj.getPartition(i).getName(), 24); // nome del settore
        }

        data[328] = 0; // ???

        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.AreasAndPartitions deserialize(byte[] data) {
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum");

        org.paternostro.elkromm.dto.AreasAndPartitions  retval = new org.paternostro.elkromm.dto.AreasAndPartitions();
        int                                             selfExclusion;
        int                                             armingBlock;
        Partition.Type                                  type;
        byte                                            partitionBitMask;

        retval.setAreaNum(data[0]); // numero di aree

        for (byte i = 0; i < ElkrommFacade.MAX_AREAS; i++) {
            retval.addArea(new Area(ElkrommUtils.getText(data, 5 + i * 24, 24), ElkrommUtils.unpackPartitions(data[i + 1])));
        }

        retval.setPartitionNum(data[101]); // numero di settori
        selfExclusion = data[102];
        armingBlock = data[103];

        for (byte i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            partitionBitMask = ElkrommFacade.Partition.values()[i].getValue();

            if ((selfExclusion & partitionBitMask) == 0x00) {
                if ((armingBlock & partitionBitMask) == 0x00) {
                    type = Partition.Type.STANDARD;
                } else {
                    type = Partition.Type.ARMING_BLOCK;
                }
            } else {
                if ((armingBlock & partitionBitMask) == 0x00) {
                    type = Partition.Type.SELF_EXCLUSION;
                } else {
                    type = Partition.Type.UNKNOWN;
                }
            }

            retval.addPartition(new Partition(ElkrommUtils.getText(data, 136 + i * 24, 24), false, type, ElkrommUtils.getWord(data, 104 + i * 2), ElkrommUtils.getWord(data, 120 + i * 2)));
        }

        return retval;
    }

    @Override
    public int length() {
        return 1+(1+ElkrommFacade.NAME_LENGTH)*ElkrommFacade.MAX_AREAS+1+1+1+(2+2+ElkrommFacade.NAME_LENGTH)*ElkrommFacade.MAX_PARTITIONS+1+4; // 333 w/ checksum
    }
}
