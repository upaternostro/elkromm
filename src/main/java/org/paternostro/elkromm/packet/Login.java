package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.ElkronCommand;

public class Login extends ElkrommPacket {
    public Login(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.LOGIN.getValue(), data);

        assert totalPackets == 0 : "Login: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Login: index: expected 0, found " + index;
        assert dataLength == 7 : "Login: dataLength: expected 7, found " + dataLength;
        assert dataLength == 7 && data != null && data.length == 7 : "Login: data: mismatch in data length";
    }

    public int getPlantCode() {
        return ElkrommUtils.dcbByte(data[0]) * 1000000 + ElkrommUtils.dcbByte(data[1]) * 10000 + ElkrommUtils.dcbByte(data[2]) * 100 + ElkrommUtils.dcbByte(data[3]);
    }

    public int getTechnicalCode() {
        return ElkrommUtils.dcbByte(data[4]) * 10000 + ElkrommUtils.dcbByte(data[5]) * 100 + ElkrommUtils.dcbByte(data[6]);
    }
}
