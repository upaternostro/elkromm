package org.paternostro.elkromm;

import java.util.Arrays;

public class ElkrommUtils
{
    public static String getText(byte[] data, int startingOffset, int maxLength)
    {
        int lastOffset = startingOffset;

        for (; lastOffset < startingOffset + maxLength; lastOffset++) {
            if (data[lastOffset] == 0x00) {
                break;
            }
        }

        return new String(Arrays.copyOfRange(data, startingOffset, lastOffset));
    }

    public static int getWord(byte[] data, int startingOffset)
    {
//        return data[startingOffset] << 8 | data[startingOffset + 1];
        return (int)(((long)data[startingOffset]) & 0xFFL << 8 | ((long)data[startingOffset + 1]) & 0xFFL);
//        return (int)(((long)data[startingOffset + 1]) & 0xFFL << 8 | ((long)data[startingOffset]) & 0xFFL);
    }

    public static int getLong(byte[] data, int startingOffset)
    {
        return data[startingOffset] << 24 | data[startingOffset + 1] << 16 | data[startingOffset + 2] << 8 | data[startingOffset + 3];
//        return (int)(((long)data[startingOffset]) & 0xFFL << 24 | ((long)data[startingOffset + 1]) & 0xFFL << 16 | ((long)data[startingOffset + 2]) & 0xFFL << 8 | ((long)data[startingOffset + 3]) & 0xFFL);
    }

    public static boolean[] unpackPartitions(byte data)
    {
        boolean[]   partitions = new boolean[ElkrommFacade.MAX_PARTITIONS];

        for (ElkrommFacade.Partition partition : ElkrommFacade.Partition.values()) {
            partitions[partition.ordinal()] = (data & partition.getBitMask()) != 0x00;
        }

        return partitions;
    }
}
