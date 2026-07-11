package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

public class SingleCredential implements Serializable {
    private byte        index;
    private Credential  credential;

    public SingleCredential(byte index, Credential credential) {
        setIndex(index);
        setCredential(credential);
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        if (index < 1 || index > 32) throw new IllegalArgumentException("Wrong index " + index + ", expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.index = index;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        if (credential == null) throw new IllegalArgumentException("Missing mandatory credential");

        this.credential = credential;
    }
}
