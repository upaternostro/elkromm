package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.Partition;

public class AreasAndPartitions implements ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions>
{
    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.AreasAndPartitions obj) {
        byte[]  data = new byte[length()];

        data[0] = (byte)obj.getAreaNum(); // numero di aree

        for (int i = 0; i < obj.getAreaNum(); i++) {
            data[i + 1] = (byte)ElkrommUtils.packPartitions(obj.getArea(i).getAssociatedPartitions()); // settori nell'area i-esima (bit mask)
            ElkrommUtils.setText(data, 5 + i*24, obj.getArea(i).getName(), 24); // nome dell'area i-esima
        }
        
        data[101] = (byte)obj.getPartitionNum(); // numero di settori
        data[102] = 0; // self exclusion (bit mask)
        data[103] = 0; // arming block (bit mask)

        for (int i = 0; i < obj.getPartitionNum(); i++) {
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
        int                                             areas;
        int                                             partitions;
        int                                             selfExclusion;
        int                                             armingBlock;
        Partition.Type                                  type;
        byte                                            partitionBitMask;

        areas = data[0]; // numero di aree

        for (byte i = 0; i < areas; i++) {
            retval.addArea(new Area(i + 1, ElkrommUtils.getText(data, 5 + i * 24, 24), ElkrommUtils.unpackPartitions(data[i + 1])));
        }

        partitions = data[101]; // numero di settori
        selfExclusion = data[102];
        armingBlock = data[103];

        for (byte i = 0; i < partitions; i++) {
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

            retval.addPartition(new Partition(i + 1, ElkrommUtils.getText(data, 136 + i * 24, 24), false, type, ElkrommUtils.getWord(data, 104 + i * 2), ElkrommUtils.getWord(data, 120 + i * 2)));
        }

        return retval;
    }

    @Override
    public int length() {
        return 1+(1+ElkrommFacade.NAME_LENGTH)*ElkrommFacade.MAX_AREAS+1+1+1+(2+2+ElkrommFacade.NAME_LENGTH)*ElkrommFacade.MAX_PARTITIONS+1+4; // 333 w/ checksum
    }
}
