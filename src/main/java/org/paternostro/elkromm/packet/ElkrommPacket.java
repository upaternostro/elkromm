package org.paternostro.elkromm.packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.paternostro.elkromm.ElkrommException;
import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkrommPacket {
    public static final Logger logger = LoggerFactory.getLogger(ElkrommPacket.class);

    public enum Direction {
        TO_CLIENT,
        FROM_CLIENT
    }

    protected Direction     direction;
    protected int           plantCode12;
    protected int           plantCode34;
    protected int           totalPackets;
    protected int           index;
    protected int           dataLength;
    protected ElkronCommand command;
    protected byte[]        data;

    public ElkrommPacket(Direction direction, int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, int command, byte[] data) {
        assert direction != null : "ElkrommPacket: missing mandatory direction";
        assert totalPackets >= 0 && totalPackets < 256 : "ElkrommPacket: totalPackets: expected [0,255], found " + totalPackets;
        assert index >= 0 && index <= totalPackets : "ElkrommPacket: index: expected [0," + totalPackets + "], found " + index;
        assert dataLength >= 0 && dataLength < 256 : "ElkrommPacket: dataLength: expected [0,255], found " + dataLength;
        assert command > 0 && command < 256 : "ElkrommPacket: command: expected [1,255], found " + command;
        assert (dataLength == 0 && (data == null || data.length == 0)) || (dataLength > 0 && data != null && data.length == dataLength) : "ElkrommPacket: data: mismatch in data length";
        
        this.direction      = direction;
        this.plantCode12    = plantCode12;
        this.plantCode34    = plantCode34;
        this.totalPackets   = totalPackets;
        this.index          = index;
        this.dataLength     = dataLength;
        this.command        = ElkronCommand.valueOf(command);
        this.data           = data;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getPlantCode12() {
        return plantCode12;
    }

    public int getPlantCode34() {
        return plantCode34;
    }

    public int getTotalPackets() {
        return totalPackets;
    }

	public int getIndex() {
        return index;
	}

    public int getDataLength() {
        return dataLength;
    }

    public ElkronCommand getCommand() {
        return command;
    }

    public byte[] getData() {
        return data;
    }

    protected int computeChecksum(byte[] inner) {
        int     checksum = 0x10000;

        if (inner != null && inner.length > 7 && (inner[6] & 0xFF) == ElkronCommand.ENABLE_DISABLE_USER.getValue()) {
            logger.warn("computeChecksum: command is ENABLE_DISABLE_USER, dataLength is {}, data is {}", inner[4], ElkrommUtils.bytesToHex(inner));
        }

        // Last 2 bytes are the old checksum, exclude those
        for (int i = 0; i < inner.length - 2; i++) {
            checksum -= inner[i] & 0xFF;
            // if (inner[i] < 0) checksum -= 256;
        }

        return checksum;
    }

    protected byte[] buildInnerArrayWOChecksum() {
        byte[]  retval = new byte[dataLength + 9]; // 7 header bytes + 2 checksum bytes

        // Setup header
        retval[0] = (byte)plantCode12;
        retval[1] = (byte)plantCode34;
        retval[2] = (byte)totalPackets;
        retval[3] = (byte)index;
        retval[4] = (byte)dataLength;
        retval[5] = 0x00; // FIXME: WTF?
        retval[6] = (byte)command.getValue();

        // Copy data if any
        if (dataLength > 0) System.arraycopy(data, 0, retval, 7, dataLength);

        // Checksum (uncomputed)
        retval[dataLength + 7] = 0;
        retval[dataLength + 8] = 0;

        return retval;
    }

    protected byte[] buildInnerArray() {
        byte[]  retval = buildInnerArrayWOChecksum();
        int     checksum = computeChecksum(retval);

        retval[retval.length - 2] = (byte)((checksum & 0xFF00) >> 8);
        retval[retval.length - 1] = (byte)(checksum & 0x00FF);

        return retval;
    }

    protected byte[] escapeInnerArray() {
        byte[]  inner = buildInnerArray();
        byte[]  retval = inner;
        int     escape = 0;
        boolean escapeSOH = getDirection() == ElkrommPacket.Direction.FROM_CLIENT;

        for (byte pivot : inner) {
            if ((escapeSOH && pivot == ElkrommFacade.BYTE_SOH) || pivot == ElkrommFacade.BYTE_ETX || pivot == ElkrommFacade.BYTE_DC1) {
                escape++;
            }
        }

        if (escape > 0) {
            int i = 0;

            retval = new byte[inner.length + escape];

            for (byte pivot : inner) {
                if ((escapeSOH && pivot == ElkrommFacade.BYTE_SOH) || pivot == ElkrommFacade.BYTE_ETX || pivot == ElkrommFacade.BYTE_DC1) {
                    retval[i++] = ElkrommFacade.BYTE_DC1;
                }

                retval[i++] = pivot;
            }
        }

        return retval;
    }

    public void serialize(OutputStream os) throws IOException {
        logger.debug("Serializing " + this);

        os.write(ElkrommFacade.BYTE_SOH);
        os.write(escapeInnerArray());
        os.write(ElkrommFacade.BYTE_ETX);
    }

    protected enum PacketStatus {
        PS_NONE,
        PS_SOH,
        PS_55_1,
        PS_55_2,
        PS_NUM_PKTS,
        PS_PKT_PROGR,
        PS_DATA_LEN,
        PS_00,
        PS_CMD,
        PS_DATA,
        PS_CKSUM_1,
        PS_CKSUM_2,
        PS_ETX
    }

    public static ElkrommPacket packetFactoryAllocate(ElkronCommand cmd, int plantCode12, int plantCode34, int numPkts, int pktProgr, int dataLen, byte[] data) throws ElkrommException {
        if (cmd.getPacketClass() == null) {
            logger.warn("Allocating GENERIC for: " + cmd);
            return new ElkrommPacket(ElkrommPacket.Direction.FROM_CLIENT, plantCode12, plantCode34, numPkts, pktProgr, dataLen, cmd.getValue(), data);
        }

        ElkrommPacket   retval;
        Class<?>[]      constructorParametersClasses = new Class<?>[6];
        Object[]        constructorParametersValues = new Object[6];

        constructorParametersClasses[0] = int.class;
        constructorParametersClasses[1] = int.class;
        constructorParametersClasses[2] = int.class;
        constructorParametersClasses[3] = int.class;
        constructorParametersClasses[4] = int.class;
        constructorParametersClasses[5] = byte[].class;

        // Let's Java box these ints
        constructorParametersValues[0] = plantCode12;
        constructorParametersValues[1] = plantCode34;
        constructorParametersValues[2] = numPkts;
        constructorParametersValues[3] = pktProgr;
        constructorParametersValues[4] = dataLen;
        constructorParametersValues[5] = data;
        
        try {
            retval = cmd.getPacketClass().getConstructor(constructorParametersClasses).newInstance(constructorParametersValues);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.error("Error allocating packet for: " + cmd, e);
            throw new ElkrommException("Error allocating packet for: " + cmd, e);
        }

        return retval;
    }

    public static ElkrommPacket deserialize(InputStream is) throws ElkrommException {
        PacketStatus    packetStatus = PacketStatus.PS_NONE;
        int             inputData,
                        dataIndex = 0,
                        dataToRead = 0,
                        checksum = 0,
                        cmd = 0;
        byte            plantCode12 = 0,
                        plantCode34 = 0,
                        numPkts = 0,
                        pktProgr = 0,
                        dataLen = 0;
        byte[]          data = null;
        ElkrommPacket   retval = null;

        try {
            while ((inputData = is.read()) != -1) { // Read data until the client closes the connection
                if (inputData == ElkrommFacade.BYTE_DC1) {
                    if ((inputData = is.read()) == -1) {
                        break; // End of stream reached
                    }
                    // else: deescaped!
                }

                switch (packetStatus) {
                    case PS_NONE:
                        // FIXME: qui può arrivare anche 21 (NAK)
                        if (inputData == ElkrommFacade.BYTE_SOH) {
                            packetStatus = PacketStatus.PS_SOH;
                        } else {
                            logger.warn("Unexpected byte received: " + inputData + " in status: " + packetStatus);
                            throw new ElkrommException("Unexpected byte received: " + inputData);
                        }
                        break;
                    case PS_SOH:
                        // first two digits of plant code
                        plantCode12 = (byte)inputData;
                        packetStatus = PacketStatus.PS_55_1;
                        break;
                    case PS_55_1:
                        // second two digits of plant code
                        plantCode34 = (byte)inputData;
                        packetStatus = PacketStatus.PS_55_2;
                        break;
                    case PS_55_2:
                        numPkts = (byte)inputData;
                        packetStatus = PacketStatus.PS_NUM_PKTS;
                        break;
                    case PS_NUM_PKTS:
                        pktProgr = (byte)inputData;
                        packetStatus = PacketStatus.PS_PKT_PROGR;
                        break;
                    case PS_PKT_PROGR:
                        dataLen = (byte)inputData;
                        dataToRead = inputData & 0xFF;

                        // if (dataToRead > 0 && dataToRead <= ElkrommFacade.MAX_DATA_LENGTH) {
                            data = new byte[dataToRead];
                            dataIndex = 0;
                        // }
                        
                        packetStatus = PacketStatus.PS_DATA_LEN;
                        break;
                    case PS_DATA_LEN:
                        if (inputData == 0x00) {
                            packetStatus = PacketStatus.PS_00;
                        } else {
                            logger.warn("Unexpected byte received: " + inputData + " in status: " + packetStatus);
                            throw new ElkrommException("Unexpected byte received: " + inputData);
                        }
                        break;
                    case PS_00:
                        cmd = inputData;
                        logger.debug(String.format("Command received: 0x%02x enum value: %s", cmd, ElkronCommand.valueOf(cmd)));
                        packetStatus = PacketStatus.PS_CMD;
                        break;
                    case PS_CMD:
                        if (dataIndex < dataToRead) {
                            data[dataIndex++] = (byte)inputData;
                            break;
                        } else {
                            packetStatus = PacketStatus.PS_DATA;
                            // Fallthrough
                        }
                    case PS_DATA:
                        checksum = inputData << 8;
                        packetStatus = PacketStatus.PS_CKSUM_1;
                        break;
                    case PS_CKSUM_1:
                        checksum |= inputData;
                        packetStatus = PacketStatus.PS_CKSUM_2;
                        break;
                    case PS_CKSUM_2:
                        if (inputData == ElkrommFacade.BYTE_ETX) {
                            packetStatus = PacketStatus.PS_ETX;
                            // Handle complete packet here
                            retval = packetFactoryAllocate(ElkronCommand.valueOf(cmd), plantCode12, plantCode34, numPkts, pktProgr, dataLen & 0xFF, data);
                            
                            int expected = retval.computeChecksum(retval.buildInnerArrayWOChecksum());

                            if (checksum != expected) {
                                // Wrong checksum received
                                logger.warn("Unexpected checksum received: " + checksum + " expected: " + expected);
                                ElkrommUtils.dumpPayload(ElkronCommand.valueOf(cmd), data);
                                throw new ElkrommException("Unexpected checksum received: " + checksum);
                            }

                            return retval;
                        } else {
                            logger.warn("Unexpected byte received: " + inputData + " in status: " + packetStatus);
                            throw new ElkrommException("Unexpected byte received: " + inputData);
                        }
                        // Unreachable
                        // break;
                    case PS_ETX:
                    default:
                        // should never reach here, as we should have already handled the complete packet
                        logger.warn("Unexpected packet status: " + packetStatus);
                        throw new ElkrommException("Unexpected packet status: " + packetStatus);
                }
            }
        } catch (IOException e) {
            logger.error("Exception reading data", e);
            throw new ElkrommException("Exception reading data", e);
        }

        return retval;
    }

    public byte[] cachePayload(Map<ElkronCommand, SortedSet<ElkrommPacket>> cmdPayloads) {
        byte[]                      retval = null;
        SortedSet<ElkrommPacket>    payloads = cmdPayloads.get(this.getCommand());

        if (payloads == null) {
            cmdPayloads.put(this.getCommand(), payloads = new TreeSet<>(new Comparator<ElkrommPacket>() {
                @Override
                public int compare(ElkrommPacket p1, ElkrommPacket p2) {
                    return Integer.compare(p1.getIndex(), p2.getIndex());
                }
            }));
        }

        payloads.add(this);

        if (payloads.size() == this.getTotalPackets() + 1) {
            int     totalLength = 0;

            for (Iterator<ElkrommPacket> it = payloads.iterator(); it.hasNext(); ) {
                totalLength += it.next().getDataLength();
            }

            int     offset = 0;

            retval = new byte[totalLength];

            for (Iterator<ElkrommPacket> it = payloads.iterator(); it.hasNext(); ) {
                ElkrommPacket p = it.next();
                System.arraycopy(p.getData(), 0, retval, offset, p.getDataLength());
                offset += p.getDataLength();
            }

            if (logger.isDebugEnabled()) ElkrommUtils.dumpPayload(this.getCommand(), retval);
            
            cmdPayloads.remove(this.getCommand());
        }

        return retval;
    }

    @Override
    public String toString() {
        StringBuffer    sb = new StringBuffer(getClass().getName());

        sb.append(": { packet ").append(index).append("/").append(totalPackets)
            .append(", command: ").append(command)
            .append(", dataLength: ").append(dataLength)
            .append(", data: [");

        if (data != null && data.length > 0) {
            sb.append(" ").append(ElkrommUtils.bytesToHex(data)).append(" ");
        }

        sb.append("] }");
        
        return sb.toString();
    }
}