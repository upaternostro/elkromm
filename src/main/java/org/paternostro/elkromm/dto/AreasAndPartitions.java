package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    public boolean isAreasEmpty() {
        return areas.isEmpty();
    }

    public void forEachArea(Consumer<? super Area> action) {
        areas.forEach(action);
    }

    public Iterator<Area> areasIterator() {
        return areas.iterator();
    }

    public Stream<Area> areasStream() {
        return areas.stream();
    }

    public Stream<Area> areasParallelStream() {
        return areas.parallelStream();
    }

    public ListIterator<Area> areasListIterator() {
        return areas.listIterator();
    }

    public ListIterator<Area> areasListIterator(int index) {
        return areas.listIterator(index);
    }

    public Spliterator<Area> areasSpliterator() {
        return areas.spliterator();
    }

    public boolean isPartitionsEmpty() {
        return partitions.isEmpty();
    }

    public void forEachPartition(Consumer<? super Partition> action) {
        partitions.forEach(action);
    }

    public Iterator<Partition> partitionsIterator() {
        return partitions.iterator();
    }

    public Stream<Partition> partitionsStream() {
        return partitions.stream();
    }

    public Stream<Partition> partitionsParallelStream() {
        return partitions.parallelStream();
    }

    public ListIterator<Partition> partitionsListIterator() {
        return partitions.listIterator();
    }

    public ListIterator<Partition> partitionsListIterator(int index) {
        return partitions.listIterator(index);
    }

    public Spliterator<Partition> partitionsSpliterator() {
        return partitions.spliterator();
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{areas=").append(areas).append(", partitions=").append(partitions).append("}");

        return sb.toString();
    }
}
