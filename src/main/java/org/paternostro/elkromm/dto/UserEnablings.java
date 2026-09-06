package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * Which users are currently enabled.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class UserEnablings implements Serializable
{
    private boolean[]   enablings;

    /**
     * Creates a new user enablings snapshot.
     *
     * @param enablings per-user enabled flags, length {@link ElkrommFacade#MAX_CREDENTIALS}
     */
    public UserEnablings(boolean[] enablings)
    {
        setEnablings(enablings);
    }

    /**
     * Returns a copy of the per-user enabled flags.
     *
     * @return the flags, length {@link ElkrommFacade#MAX_CREDENTIALS}
     */
    public boolean[] getEnablings()
    {
        return Arrays.copyOf(enablings, ElkrommFacade.MAX_CREDENTIALS);
    }

    /**
     * Sets the per-user enabled flags.
     *
     * @param enablings the flags to set, not {@code null}
     * @throws IllegalArgumentException if {@code enablings} is {@code null}
     */
    public void setEnablings(boolean[] enablings)
    {
        if (enablings == null) throw new IllegalArgumentException("Missing mandatory enablings");

        this.enablings = Arrays.copyOf(enablings, ElkrommFacade.MAX_CREDENTIALS);
    }

    /**
     * Sets the enabled flag of a single user.
     *
     * @param ordinal 1-based ordinal of the user
     * @param enabled the flag to set
     * @throws IllegalArgumentException if {@code ordinal} is out of range
     */
    public void setEnabling(int ordinal, boolean enabled)
    {
        if (ordinal < 1 || ordinal > ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong ordinal " + ordinal + ", expected between 1 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.enablings[ordinal - 1] = enabled;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{enablings=").append(Arrays.toString(enablings)).append("}");

        return sb.toString();
    }
}
