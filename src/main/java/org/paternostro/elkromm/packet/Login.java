package org.paternostro.elkromm.packet;

import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkronCommand;

public class Login extends ElkrommPacket {
    public Login(int plantCode12, int plantCode34, int totalPackets, int index, int dataLength, byte[] data) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, totalPackets, index, dataLength, ElkronCommand.LOGIN.getValue(), data);

        assert totalPackets == 0 : "Login: totalPackets: expected 0, found " + totalPackets;
        assert index == 0 : "Login: index: expected 0, found " + index;
        assert dataLength == 7 : "Login: dataLength: expected 7, found " + dataLength;
        assert dataLength == 7 && data != null && data.length == 7 : "Login: data: mismatch in data length";
    }

    public Login(int plantCode12, int plantCode34, org.paternostro.elkromm.dto.Login login) {
        super(Direction.FROM_CLIENT, plantCode12, plantCode34, 0, 0, 7, ElkronCommand.LOGIN.getValue(), ElkrommFactory.getFactory().getLoginSerializer().serialize(login));
    }
}
