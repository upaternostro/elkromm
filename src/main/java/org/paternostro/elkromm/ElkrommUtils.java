package org.paternostro.elkromm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Low-level helpers for encoding/decoding the Elkron protocol's on-the-wire
 * byte representation: fixed-length strings, big-endian words/longs, BCD
 * digits, partition bitmasks, block checksums, and hex dumps.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class ElkrommUtils
{
    /** Logger used for protocol-level dumps and warnings. */
    public static final Logger logger = LoggerFactory.getLogger(ElkrommUtils.class);

    /**
     * Reads a fixed-length, NUL-terminated ASCII string out of a byte array.
     *
     * @param data the buffer to read from
     * @param startingOffset offset of the first character
     * @param maxLength maximum number of bytes to scan for a terminating NUL
     * @return the decoded string, up to the first NUL byte (or {@code maxLength} if none is found)
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short
     */
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

    /**
     * Writes a string into a fixed-length field, NUL-padding the remainder.
     *
     * @param data the buffer to write into
     * @param startingOffset offset of the first character
     * @param string the string to write (truncated if longer than {@code length})
     * @param length total length of the field, including padding
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short
     */
    public static void setText(byte[] data, int startingOffset, String string, int length)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + length) throw new IllegalArgumentException("Not enough data");

        Arrays.fill(data, startingOffset, startingOffset + length, (byte)0); // Pad with null bytes
        System.arraycopy(string.getBytes(), 0, data, startingOffset, Math.min(string.length(), length));
    }

    /**
     * Reads a big-endian 16-bit unsigned value.
     *
     * @param data the buffer to read from
     * @param startingOffset offset of the high byte
     * @return the decoded value, in range [0, 65535]
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short
     */
    public static int getWord(byte[] data, int startingOffset)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 2) throw new IllegalArgumentException("Not enough data");

        return (data[startingOffset] & 0xFF) << 8 | (data[startingOffset + 1] & 0xFF);
    }

    /**
     * Writes a big-endian 16-bit value.
     *
     * @param data the buffer to write into
     * @param startingOffset offset of the high byte
     * @param value the value to write (only the low 16 bits are used)
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short
     */
    public static void setWord(byte[] data, int startingOffset, long value)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 2) throw new IllegalArgumentException("Not enough data");

        data[startingOffset] = (byte)((value >> 8) & 0xFF);
        data[startingOffset + 1] = (byte)(value & 0xFF);
    }

    /**
     * Reads a big-endian 32-bit value.
     *
     * @param data the buffer to read from
     * @param startingOffset offset of the highest-order byte
     * @return the decoded value
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short
     */
    public static int getLong(byte[] data, int startingOffset)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 4) throw new IllegalArgumentException("Not enough data");

        return (data[startingOffset] & 0xFF) << 24 | (data[startingOffset + 1] & 0xFF) << 16 | (data[startingOffset + 2] & 0xFF) << 8 | (data[startingOffset + 3] & 0xFF);
    }

    /**
     * Writes a big-endian 32-bit value.
     *
     * @param data the buffer to write into
     * @param startingOffset offset of the highest-order byte
     * @param value the value to write
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short
     */
    public static void setLong(byte[] data, int startingOffset, int value)
    {
        if (data == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (data.length < startingOffset + 4) throw new IllegalArgumentException("Not enough data");

        data[startingOffset] = (byte)((value >> 24) & 0xFF);
        data[startingOffset + 1] = (byte)((value >> 16) & 0xFF);
        data[startingOffset + 2] = (byte)((value >> 8) & 0xFF);
        data[startingOffset + 3] = (byte)(value & 0xFF);
    }

    /**
     * Unpacks a {@link ElkrommFacade.Partition} bitmask byte into a
     * per-partition boolean array (index 0 = partition 1).
     *
     * @param data the bitmask byte to unpack
     * @return an array of {@link ElkrommFacade#MAX_PARTITIONS} flags
     */
    public static boolean[] unpackPartitions(byte data)
    {
        boolean[]   partitions = new boolean[ElkrommFacade.MAX_PARTITIONS];

        for (ElkrommFacade.Partition partition : ElkrommFacade.Partition.values()) {
            if (partition == ElkrommFacade.Partition.P_NONE || partition == ElkrommFacade.Partition.P_ALL) continue;

            partitions[partition.ordinal()-1] = (data & partition.getValue()) != 0x00;
        }

        return partitions;
    }

    /**
     * Packs a per-partition boolean array (index 0 = partition 1) into a
     * single {@link ElkrommFacade.Partition} bitmask byte.
     *
     * @param partitions an array of at least {@link ElkrommFacade#MAX_PARTITIONS} flags
     * @return the packed bitmask byte
     * @throws IllegalArgumentException if {@code partitions} is {@code null} or too short
     */
    public static byte packPartitions(boolean[] partitions)
    {
        if (partitions == null) throw new IllegalArgumentException("Missing mandatory parameter");
        if (partitions.length < ElkrommFacade.MAX_PARTITIONS) throw new IllegalArgumentException("Not enough data, expected " + ElkrommFacade.MAX_PARTITIONS + " partitions");

        byte    data = 0x00;

        for (ElkrommFacade.Partition partition : ElkrommFacade.Partition.values()) {
            if (partition == ElkrommFacade.Partition.P_NONE || partition == ElkrommFacade.Partition.P_ALL) continue;

            if (partitions[partition.ordinal()-1]) {
                data |= partition.getValue();
            }
        }

        return data;
    }

    /**
     * Computes the checksum used at the end of every protocol data block:
     * the two's-complement negative sum of every byte except the trailing
     * 4-byte checksum field itself.
     *
     * @param data the full block, including its trailing 4-byte checksum field
     * @return the computed checksum
     * @throws IllegalArgumentException if {@code data} is {@code null} or too short to contain a checksum
     */
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

    /**
     * Logs a hex/ASCII dump of a packet payload (16 bytes per line, in the
     * classic offset/hex/ASCII layout), and validates its trailing checksum
     * if the payload is long enough to contain one. Used while reverse
     * engineering the protocol and by the bundled emulator; a no-op if
     * {@code payload} is {@code null} or empty.
     *
     * @param cmd the command the payload belongs to, for the log header
     * @param payload the payload to dump, or {@code null}/empty to do nothing
     */
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

    /**
     * Encodes an integer as a list of BCD bytes, most significant first,
     * padding with leading zero bytes up to {@code minLen}.
     *
     * @param in the non-negative value to encode
     * @param minLen minimum number of BCD bytes in the result
     * @return the BCD-encoded value
     */
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

    /**
     * Encodes a two-digit (0-99) value as a single BCD byte.
     *
     * @param in the value to encode, in range [0, 99]
     * @return the BCD-encoded byte
     * @throws IllegalArgumentException if {@code in} is out of range
     */
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

    /**
     * Decodes a sequence of BCD bytes into an integer.
     *
     * @param data the buffer to read from
     * @param offset offset of the first (most significant) BCD byte
     * @param length number of BCD bytes to decode
     * @return the decoded value
     * @throws IllegalArgumentException if any byte is not valid BCD
     */
    public static int dcb(byte[] data, int offset, int length) {
        int retval = 0;

        while (length-- > 0) {
            retval *= 100;
            retval += dcbByte(data[offset++]);
        }

        return retval;
    }

    /**
     * Decodes a single BCD byte into a two-digit (0-99) value.
     *
     * @param bcd the BCD byte to decode
     * @return the decoded value, in range [0, 99]
     * @throws IllegalArgumentException if either nibble is not a valid BCD digit
     */
    public static int dcbByte(byte bcd) {
        int high = (bcd >> 4) & 0x0F;

        if (high > 9) throw new IllegalArgumentException("dcbByte: high nibble must be in range [0,9], found " + high);

        int low = bcd & 0x0F;

        if (low > 9) throw new IllegalArgumentException("dcbByte: lower nibble must be in range [0,9], found " + low);

        return high * 10 + low;
    }

    /**
     * Writes a single SYN ({@link ElkrommFacade#BYTE_SYN}) byte and flushes the stream.
     *
     * @param outputStream the stream to write to
     * @throws IOException if the write fails
     */
    public static void sendSYN(OutputStream outputStream) throws IOException {
        logger.debug("Sending SYN...");
        outputStream.write(ElkrommFacade.BYTE_SYN);
        outputStream.flush();
    }

    /**
     * Writes a single ACK ({@link ElkrommFacade#BYTE_ACK}) byte and flushes the stream.
     *
     * @param outputStream the stream to write to
     * @throws IOException if the write fails
     */
    public static void sendACK(OutputStream outputStream) throws IOException {
        logger.debug("Sending ACK...");
        outputStream.write(ElkrommFacade.BYTE_ACK);
        outputStream.flush();
    }

    /**
     * Writes a single NAK ({@link ElkrommFacade#BYTE_NAK}) byte and flushes the stream.
     *
     * @param outputStream the stream to write to
     * @throws IOException if the write fails
     */
    public static void sendNAK(OutputStream outputStream) throws IOException {
        logger.debug("Sending NAK...");
        outputStream.write(ElkrommFacade.BYTE_NAK);
        outputStream.flush();
    }
    
    /**
     * Converts a {@code List<Byte>} (as produced by {@link #bcd(int, int)}) into a primitive {@code byte[]}.
     *
     * @param list the list to convert
     * @return the equivalent primitive array
     */
    public static byte[] listToArray(List<Byte> list)
    {
        byte[] retval = new byte[list.size()];

        for (int i = 0; i < retval.length; i++) {
            retval[i] = list.get(i);
        }

        return retval;
    }

    // Source - https://stackoverflow.com/a/9855338
    // Posted by maybeWeCouldStealAVan, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-03-27, License - CC BY-SA 4.0

    protected static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * Converts a byte array to its uppercase hexadecimal string representation.
     * <p>
     * Adapted from a <a href="https://stackoverflow.com/a/9855338">Stack Overflow answer</a>
     * by maybeWeCouldStealAVan and the Stack Overflow community, licensed under CC BY-SA 4.0.
     *
     * @param bytes the bytes to convert
     * @return the hex string, two characters per byte, no separators
     */
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
