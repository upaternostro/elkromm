package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Credential;

public abstract class Credentials implements ElkrommSerializer<Credential[]>
{
    @Override
    public byte[] serialize(Credential[] obj) {
        byte[]  data = new byte[length()];

        for (int i = 0; i < obj.length; i++) {
            data[i*26] = obj[i].getEnabling().getValue();
            data[i*26 + 1] = ElkrommUtils.packPartitions(obj[i].getAssociatedPartitions());
            ElkrommUtils.setText(data, 2 + i*26, obj[i].getName(), 24); // nome dell'utente i-esimo
        }
        
        ElkrommUtils.setLong(data, data.length - 4, ElkrommUtils.computeBlockChecksum(data));

        return data;
    }

    @Override
    public Credential[] deserialize(byte[] data) {
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getLong(data, data.length - 4)) throw new IllegalArgumentException("Wrong checksum, expected: " + ElkrommUtils.computeBlockChecksum(data) + " found: " + ElkrommUtils.getLong(data, data.length - 4));

        Credential[]  retval = new Credential[ElkrommFacade.MAX_CREDENTIALS];

        for (byte i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            retval[i] = allocateCredential(i + 1, ElkrommUtils.getText(data, 2 + i*26, 24), Credential.Enabling.valueOf(data[i*26]), ElkrommUtils.unpackPartitions(data[i*26 + 1]));
        }

        return retval;
    }

    protected Credential allocateCredential(int ordinal, String name, Credential.Enabling enabling, boolean[] associatedPartitions)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int length() {
        return ElkrommFacade.MAX_CREDENTIALS*(1+1+ElkrommFacade.NAME_LENGTH)+4; // 32 utenti (ognuno con 2 byte di flag e 24 di nome) + 4 byte di checksum
    }
}
