package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AreasAndPartitions implements Serializable
{
    private List<Area>      areas;
    private List<Partition> partitions;

    public AreasAndPartitions()
    {
        this.areas = new ArrayList<>();
        this.partitions = new ArrayList<>();
    }

    public void addArea(Area area)
    {
        areas.add(area);
    }

    public void addPartition(Partition partition)
    {
        partitions.add(partition);
    }

    public int getAreaNum()
    {
        return areas.size();
    }

    public int getPartitionNum()
    {
        return partitions.size();
    }

    public Area getArea(int index)
    {
        return areas.get(index);
    }

    public Partition getPartition(int index)
    {
        return partitions.get(index);
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{areas=").append(areas).append(", partitions=").append(partitions).append("}");

        return sb.toString();
    }
}
