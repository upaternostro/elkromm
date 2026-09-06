package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A single {@link Credential} (user or key) paired with its index, for the
 * single-instance write commands ({@code USER_PROGRAMMING}/{@code KEY_PROGRAMMING}).
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SingleCredential implements Serializable {
    private byte        index;
    private Credential  credential;

    /**
     * Creates a new single-credential write.
     *
     * @param index 1-based position of the credential in its array
     * @param credential the credential data (a {@link User} or a {@link Key})
     */
    public SingleCredential(byte index, Credential credential) {
        setIndex(index);
        setCredential(credential);
    }

    /**
     * Returns the 1-based index of this credential.
     *
     * @return the index
     */
    public byte getIndex() {
        return index;
    }

    /**
     * Sets the 1-based index of this credential.
     *
     * @param index the index to set, in range [1, {@link ElkrommFacade#MAX_CREDENTIALS}]
     * @throws IllegalArgumentException if out of range
     */
    public void setIndex(byte index) {
        if (index < 1 || index > ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong index " + index + ", expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.index = index;
    }

    /**
     * Returns the credential data.
     *
     * @return the credential (a {@link User} or a {@link Key})
     */
    public Credential getCredential() {
        return credential;
    }

    /**
     * Sets the credential data.
     *
     * @param credential the credential to set, not {@code null}
     * @throws IllegalArgumentException if {@code credential} is {@code null}
     */
    public void setCredential(Credential credential) {
        if (credential == null) throw new IllegalArgumentException("Missing mandatory credential");

        this.credential = credential;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{index=").append(index).append(", credential=").append(credential).append("}");

        return sb.toString();
    }
}
