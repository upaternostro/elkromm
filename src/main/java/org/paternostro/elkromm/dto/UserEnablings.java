package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class UserEnablings implements Serializable
{
    private boolean[]   enablings;

    public UserEnablings(boolean[] enablings)
    {
        setEnablings(enablings);
    }

    public boolean[] getEnablings()
    {
        return Arrays.copyOf(enablings, ElkrommFacade.MAX_CREDENTIALS);
    }

    public void setEnablings(boolean[] enablings)
    {
        if (enablings == null) throw new IllegalArgumentException("Missing mandatory enablings");

        this.enablings = Arrays.copyOf(enablings, ElkrommFacade.MAX_CREDENTIALS);
    }

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
