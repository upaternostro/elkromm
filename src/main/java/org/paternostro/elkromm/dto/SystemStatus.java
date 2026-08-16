package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.Arrays;

import org.paternostro.elkromm.ElkrommFacade;

public class SystemStatus implements Serializable
{
    protected boolean[] activePartitions;

    public SystemStatus(boolean[] activePartitions)
    {
        setActivePartitions(activePartitions);
    }

    public boolean[] getActivePartitions()
    {
        return Arrays.copyOf(activePartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public void setActivePartitions(boolean[] activePartitions)
    {
        if (activePartitions == null) throw new IllegalArgumentException("Missing mandatory active partitions");

        this.activePartitions = Arrays.copyOf(activePartitions, ElkrommFacade.MAX_PARTITIONS);
    }

    public void setPartitionArming(ElkrommFacade.Partition partition, boolean arming)
    {
        this.activePartitions[partition.ordinal() - 1] = arming;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{activePartitions=").append(Arrays.toString(activePartitions)).append("}");

        return sb.toString();
    }
}
