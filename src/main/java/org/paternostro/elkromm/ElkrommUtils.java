package org.paternostro.elkromm;

import java.util.Arrays;

public class ElkrommUtils
{
    public static String getText(byte[] data, int startingOffset, int maxLength)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + maxLength) throw new IllegalArgumentException("Not enough data");

        int lastOffset = startingOffset;

        for (; lastOffset < startingOffset + maxLength; lastOffset++) {
            if (data[lastOffset] == 0x00) {
                break;
            }
        }

        return new String(Arrays.copyOfRange(data, startingOffset, lastOffset));
    }

    public static void setText(byte[] data, int startingOffset, String string, int length)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + length) throw new IllegalArgumentException("Not enough data");

        Arrays.fill(data, startingOffset, startingOffset + length, (byte)0); // Pad with null bytes
        System.arraycopy(string.getBytes(), 0, data, startingOffset, Math.min(string.length(), length));
    }

    public static int getWord(byte[] data, int startingOffset)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 2) throw new IllegalArgumentException("Not enough data");

        return (data[startingOffset] & 0xFF) << 8 | (data[startingOffset + 1] & 0xFF);
    }

    public static void setWord(byte[] data, int startingOffset, long value)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 2) throw new IllegalArgumentException("Not enough data");

        data[startingOffset] = (byte)((value >> 8) & 0xFF);
        data[startingOffset + 1] = (byte)(value & 0xFF);
    }

    public static int getLong(byte[] data, int startingOffset)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 4) throw new IllegalArgumentException("Not enough data");

        return (data[startingOffset] & 0xFF) << 24 | (data[startingOffset + 1] & 0xFF) << 16 | (data[startingOffset + 2] & 0xFF) << 8 | (data[startingOffset + 3] & 0xFF);
    }

    public static void setLong(byte[] data, int startingOffset, int value)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 4) throw new IllegalArgumentException("Not enough data");

        data[startingOffset] = (byte)((value >> 24) & 0xFF);
        data[startingOffset + 1] = (byte)((value >> 16) & 0xFF);
        data[startingOffset + 2] = (byte)((value >> 8) & 0xFF);
        data[startingOffset + 3] = (byte)(value & 0xFF);
    }

    public static boolean[] unpackPartitions(byte data)
    {
        boolean[]   partitions = new boolean[ElkrommFacade.MAX_PARTITIONS];

        for (ElkrommFacade.Partition partition : ElkrommFacade.Partition.values()) {
            partitions[partition.ordinal()] = (data & partition.getBitMask()) != 0x00;
        }

        return partitions;
    }

    public static byte packPartitions(boolean[] partitions)
    {
        if (partitions == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (partitions.length < ElkrommFacade.MAX_PARTITIONS) throw new IllegalArgumentException("Not enough data, expected " + ElkrommFacade.MAX_PARTITIONS + " partitions");

        byte    data = 0x00;

        for (ElkrommFacade.Partition partition : ElkrommFacade.Partition.values()) {
            if (partitions[partition.ordinal()]) {
                data |= partition.getBitMask();
            }
        }

        return data;
    }

    public static int computeBlockChecksum(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length <= 4) throw new IllegalArgumentException("Not enough data to compute checksum");

        int checksum = 0;

        for (int i = 0; i < data.length - 4; i++) {
            checksum -= data[i] & 0xFF;
        }

        return checksum;
    }

    public static void dumpPayload(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return;
        }

        StringBuffer    sb = new StringBuffer();
        StringBuffer    sb2 = new StringBuffer();
        int             i;
        int             checksum = payload.length > 4 ? getLong(payload, payload.length - 4) : 0;

        for (i = 0; i < payload.length; ) {
            if (i % 16 == 0) {
                sb.append(String.format("%04x: ", i));
            }

            sb.append(String.format("%02x ", payload[i]));    
            sb2.append((Character.isLetterOrDigit((char)payload[i]) || !Character.isISOControl((char)payload[i])) ? (char)payload[i] : '.');

            i++;

            if (i % 8 == 0) {
                sb.append(" ");
                sb2.append(" ");
            }
            
            if (i % 16 == 0) {
                sb.append(" ").append(sb2);
                System.out.println(sb.toString());
                sb.setLength(0); // Clear the buffer
                sb2.setLength(0); // Clear the buffer
            }
        }

        if (i > 0) {
            while (i % 16 != 0) {
                sb.append("   ");

                if (i % 8 == 7) {
                    sb.append(" ");
                }

                i++;
            }

            sb.append(" ").append(sb2);
            System.out.println(sb.toString());
        }

        if (payload.length > 4) {
            if (checksum == computeBlockChecksum(payload)) {
                System.out.println(String.format("Checksum %08x is valid", checksum));
            } else {
                System.err.println(String.format("Checksum %08x is invalid", checksum));
            }
        }
    }
}
