package org.paternostro.elkromm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkrommUtils
{
    public static final Logger logger = LoggerFactory.getLogger(ElkrommUtils.class);

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

    public static void dumpPayload(ElkronCommand cmd, byte[] payload) {
        if (payload == null || payload.length == 0) {
            return;
        }

        StringBuffer    sb = new StringBuffer();
        StringBuffer    sb2 = new StringBuffer();
        int             i;
        int             checksum = payload.length > 4 ? getLong(payload, payload.length - 4) : 0;

        logger.info("Dumping " + cmd + ", payload size: " + payload.length);

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
                logger.info(sb.toString());
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
            logger.info(sb.toString());
        }

        if (payload.length > 4) {
            if (checksum == computeBlockChecksum(payload)) {
                logger.info(String.format("Checksum %08x is valid", checksum));
            } else {
                logger.warn(String.format("Checksum %08x is invalid", checksum));
            }
        }
    }

    public static List<Byte> bcd(int in, int minLen)
    {
        List<Byte> retval = new ArrayList<>();

        while (in > 0) {
            retval.add(0, bcdByte(in % 100));
            in /= 100;
        }

        while (retval.size() < minLen) {
            retval.add(0, (byte)0x00);
        }

        return retval;
    }

    public static byte bcdByte(int in)
    {
        if (in < 0 || in > 99) {
            throw new IllegalArgumentException("bcdByte: input must be in range [0,99], found " + in);
        }

        int lower = in % 10;
        in /= 10;
        int upper = in % 10;

        return (byte)(upper << 4 | lower);
    }

    public static int dcb(byte[] data, int offset, int length) {
        int retval = 0;

        while (length-- > 0) {
            retval *= 100;
            retval += dcbByte(data[offset++]);
        }

        return retval;
    }

    public static int dcbByte(byte bcd) {
        int high = (bcd >> 4) & 0x0F;

        if (high > 9) throw new IllegalArgumentException("dcbByte: high nibble must be in range [0,9], found " + high);

        int low = bcd & 0x0F;

        if (low > 9) throw new IllegalArgumentException("dcbByte: lower nibble must be in range [0,9], found " + low);

        return high * 10 + low;
    }

    public static void sendSYN(OutputStream outputStream) throws IOException {
        logger.debug("Sending SYN...");
        outputStream.write(ElkrommFacade.BYTE_SYN);
        outputStream.flush();
    }

    public static void sendACK(OutputStream outputStream) throws IOException {
        logger.debug("Sending ACK...");
        outputStream.write(ElkrommFacade.BYTE_ACK);
        outputStream.flush();
    }

    public static void sendNAK(OutputStream outputStream) throws IOException {
        logger.debug("Sending NAK...");
        outputStream.write(ElkrommFacade.BYTE_NAK);
        outputStream.flush();
    }
    
    // Source - https://stackoverflow.com/a/9855338
    // Posted by maybeWeCouldStealAVan, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-03-27, License - CC BY-SA 4.0

    protected static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String bytesToHex(byte[] bytes) {
        byte[]  hexChars = new byte[bytes.length * 2];
        int     v;

        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;

            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
